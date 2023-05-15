package org.jeecg.modules.producer.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.config.mqtoken.UserTokenContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.polymerize.api.IPolymerizeAPI;
import org.jeecg.modules.polymerize.constant.PolymerizeCacheConstant;
import org.jeecg.modules.polymerize.entity.Crawl;
import org.jeecg.modules.polymerize.entity.InformationSource;
import org.jeecg.modules.polymerize.entity.InformationSourceRule;
import org.jeecg.modules.polymerize.entity.InformationSourceTask;
import org.jeecg.modules.polymerize.model.InformationSourceJobConfigModel;
import org.jeecg.modules.polymerize.util.PolymerizeRedisUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.jeecg.common.util.oConvertUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0
 * @description:  任务生产者
 * @author: wayne
 * @date 2023/5/11 21:28
 */
@Slf4j
@Component
public class ProducerByTaskJobHandler {

    @Resource
    private IPolymerizeAPI polymerizeAPI;

    @Resource
    private PolymerizeRedisUtil polymerizeRedisUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 任务key过期时间24小时
    private long redisKeyExpire = 60 * 60 * 24;

    @XxlJob(value = "producerByTaskJobHandler")
    public void producerByTaskJobHandler() {
        // 当前执行的jobId
        long jobId = XxlJobHelper.getJobId();
        String jobIdStr = String.valueOf(jobId);
        // 信源任务队列 redisKey,用jobId拼接
        String redisKey = PolymerizeCacheConstant.INFORMATION_SOURCE_JOB_LIST_PRE + ":" + jobIdStr;
        try {
            // 设置线程会话Token
            UserTokenContext.setToken(getTemporaryToken());
            // 获取任务执行参数 (taskId用逗号间隔)
            String jobParam = XxlJobHelper.getJobParam();
            if (oConvertUtils.isEmpty(jobParam)) {
                throw new Exception("没有指定 taskId");
            }
            List<String> taskIdList = Arrays.asList(jobParam.split(","));
            if (oConvertUtils.listIsEmpty(taskIdList)) {
                throw new Exception("taskId 参数不正确");
            }
            addLog("任务 jobId=" + jobIdStr + ", 执行参数 param=" + jobParam);
            addLog("taskIdList: " + taskIdList.toString());
            // 根据taskId获取对应task的配置
            for (String taskId: taskIdList) {
                InformationSourceTask task = polymerizeAPI.queryTaskById(taskId);
                if (task == null) {
                    throw new Exception("taskId=" + taskId + ", taskName=" + task.getName() + ", 数据为null");
                }
                addLog("task [" + taskId + "] 配置: " + task.toString());
                // 根据crawlId获取爬虫的配置
                String crawlId = task.getCrawlId();
                Crawl crawl = polymerizeAPI.queryCrawlById(crawlId);
                if (crawl == null) {
                    throw new Exception("crawlId=" + crawlId + ", crawlName=" + crawl.getName() + ", 数据为null");
                }
                addLog("crawlId= " + crawlId + ", crawlName=" + crawl.getName() + ", 配置: " + crawl.toString());
                // 遍历task配置的信源,获取InformationSource的配置
                List<String> informationSourceIdList = Arrays.asList(task.getContent().split(","));
                if (oConvertUtils.listIsEmpty(informationSourceIdList)) {
                    throw new Exception("taskId=" + taskId + ", taskName=" + task.getName() + ", 没有配置信源");
                }

                for (String id : informationSourceIdList) {
                    // 根据ID获取信源配置
                    InformationSource informationSource = polymerizeAPI.queryInformationSourceById(id);
                    if (informationSource == null) {
                        throw new Exception("informationSourceId=" + id + ", informationSourceName=" + informationSource.getName() + " 数据为null");
                    }
                    addLog("informationSourceId=" + id + ", informationSourceName= " + informationSource.getName() + ", 配置: " + informationSource.toString());
                    // 根据ID获取信源规则
                    InformationSourceRule rule = polymerizeAPI.queryRuleByInformationSourceId(informationSource.getId());
                    if (rule == null) {
                        throw new Exception("informationSourceId=" + id + ", informationSourceName=" + informationSource.getName() + " 没有配置规则");
                    }
                    addLog("rule [" + rule.getId() + "] 配置: " + rule.getDrawflowConfig());
                    // 拼装redis中信源池配置
                    InformationSourceJobConfigModel model = new InformationSourceJobConfigModel();
                    model.setJobId(jobId);
                    model.setTaskId(taskId);
                    model.setTaskName(task.getName());
                    model.setCrawlId(crawlId);
                    model.setCrawlName(crawl.getName());
                    model.setRepository(crawl.getRepository());
                    model.setRunCommand(crawl.getRunCommand());
                    model.setBranch(crawl.getBranch());
                    model.setVersion(crawl.getVersion());
                    model.setInformationSourceId(id);
                    model.setInformationSourceName(informationSource.getName());
                    model.setDomain(informationSource.getDomain());
                    model.setScheme(informationSource.getScheme());
                    model.setPort(informationSource.getPort());
                    model.setRule(rule.getDrawflowConfig());
                    // 写入队列
                    if (!polymerizeRedisUtil.lPush(redisKey, model, redisKeyExpire)) {
                        throw new Exception("informationSourceId=" + id + ", informationSourceName=" + informationSource.getName() + ", 加入redis [" + redisKey + "]任务队列失败");
                    }
                }
                addLog("任务 taskId=" + taskId + ", taskName=" + task.getName() + ", 执行完成");
            }
            log.info("job jobId=" + jobId + ", 执行完成");
            XxlJobHelper.handleSuccess("job jobId=" + jobId + ", 执行完成");
            return;
        } catch (Exception e) {
            // 删除redis已经写入的任务队列
            polymerizeRedisUtil.del(redisKey);
            e.printStackTrace();
            log.error(e.getMessage());
            XxlJobHelper.handleFail( e.getMessage());
            return;
        }
    }

    private void addLog(String msg) {
        log.info(msg);
        XxlJobHelper.log(msg);
    }

    /**
     * 获取临时令牌
     *
     * 模拟登陆接口，获取模拟 Token
     * @return
     */
    public static String getTemporaryToken() {
        PolymerizeRedisUtil polymerizeRedisUtil = SpringContextUtils.getBean(PolymerizeRedisUtil.class);
        // 模拟登录生成Token
        String token = JwtUtil.sign("admin", "123456");
        // 设置Token缓存有效时间为 5 分钟
        polymerizeRedisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        polymerizeRedisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, 5 * 60 * 1000);
        return token;
    }

}
