package org.jeecg.modules.taskjob.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.jeecg.common.config.mqtoken.UserTokenContext;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.api.IPolymerizeAPI;
import org.jeecg.modules.polymerize.entity.Crawl;
import org.jeecg.modules.polymerize.entity.InformationSource;
import org.jeecg.modules.polymerize.entity.InformationSourceRule;
import org.jeecg.modules.polymerize.entity.InformationSourceTask;
import org.jeecg.modules.polymerize.model.InformationSourceJobConfigModel;
import org.jeecg.modules.polymerize.util.PolymerizeRedisUtil;
import org.jeecg.modules.taskjob.service.IAuthService;
import org.jeecg.modules.taskjob.service.IProducerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import tech.powerjob.common.serialize.JsonUtils;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.log.OmsLogger;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0
 * @description: ProducerService 生产者服务,数据库读取信源任务,写入信源任务队列
 * @author: wayne
 * @date 2023/5/23 9:57
 */
@Slf4j
@RefreshScope
@Component
public class ProducerServiceImpl implements IProducerService {

    @Resource
    private IPolymerizeAPI polymerizeAPI;

    @Resource
    private IAuthService authService;

    @Resource
    private PolymerizeRedisUtil polymerizeRedisUtil;

    @Value("${taskjob.redis.informationSourceQueueKeyPre}")
    private String redisQueueKeyPre;

    @Value("${taskjob.redis.informationSourceQueueExpire}")
    private long redisQueueExpire;

    @Value("${polymerize.ipProxyApi}")
    private String ipProxyApi;

    /**
     * 执行Producer任务
     *
     * @param jobId
     * @param taskIds
     * @param instanceId
     * @param omsLogger
     * @return
     * @throws Exception
     */
    @Override
    public void doJob(long jobId, String taskIds, long instanceId, OmsLogger omsLogger) throws Exception {
        // redis信源队列key,使用instanceId作为队列唯一标识
        String redisQueueKey = redisQueueKeyPre + ":" + String.valueOf(instanceId);
        try {
            // 设置线程会话Token
            UserTokenContext.setToken(authService.getTemporaryToken());
            List<String> taskIdList = Arrays.asList(taskIds.split(","));
            if (oConvertUtils.listIsEmpty(taskIdList)) {
                throw new Exception("taskId 参数不正确");
            }
            omsLogger.info("开始遍历job配置的taskIds,获取task与crawl的配置");
            log.info("开始遍历job配置的taskIds,获取task与crawl的配置");
            for (String taskId: taskIdList) {
                // 读取task配置
                InformationSourceTask task = polymerizeAPI.queryTaskById(taskId);
                if (task == null) {
                    throw new Exception("taskId=" + taskId + ", taskName=" + task.getName() + ", 数据为null");
                }
                omsLogger.info("读取task, taskId={}, taskName={}, 配置信息: {}", taskId, task.getName(), task.toString());
                log.info("读取task, taskId={}, taskName={}, 配置信息: {}", taskId, task.getName(), task.toString());
                // 根据task中crawlId获取爬虫的配置
                String crawlId = task.getCrawlId();
                Crawl crawl = polymerizeAPI.queryCrawlById(crawlId);
                if (crawl == null) {
                    throw new Exception("crawlId=" + crawlId + ", crawlName=" + crawl.getName() + ", 数据为null");
                }
                omsLogger.info("读取crawl, crawlId={}, crawlName={}, 配置信息: {}", crawlId, crawl.getName(), crawl.toString());
                log.info("读取crawl, crawlId={}, crawlName={}, 配置信息: {}", crawlId, crawl.getName(), crawl.toString());

                // 遍历task配置的信源,获取InformationSource的配置
                List<String> informationSourceIdList = Arrays.asList(task.getContent().split(","));
                if (oConvertUtils.listIsEmpty(informationSourceIdList)) {
                    throw new Exception("taskId=" + taskId + ", taskName=" + task.getName() + ", 没有配置信源");
                }
                omsLogger.info("开始遍历task中配置的信源列表,读取信源规则配置,写入信源队列: {}", redisQueueKey);
                log.info("开始遍历task中配置的信源列表,读取信源规则配置,写入信源队列: {}", redisQueueKey);
                for (String id : informationSourceIdList) {
                    // 根据ID获取信源配置
                    InformationSource informationSource = polymerizeAPI.queryInformationSourceById(id);
                    if (informationSource == null) {
                        throw new Exception("informationSourceId=" + id + ", informationSourceName=" + informationSource.getName() + ", 数据为null");
                    }
                    // 根据ID获取信源规则
                    InformationSourceRule rule = polymerizeAPI.queryRuleByInformationSourceId(informationSource.getId());
                    if (rule == null) {
                        throw new Exception("informationSourceId=" + id + ", informationSourceName=" + informationSource.getName() + " 没有配置规则");
                    }
                    omsLogger.info("读取信源, informationSourceId={}, informationSourceName={}, 配置信息: {}, 规则: {}", id, informationSource.getName(), informationSource.toString(), rule.getDrawflowConfig());
                    log.info("读取信源, informationSourceId={}, informationSourceName={}, 配置信息: {}, 规则: {}", id, informationSource.getName(), informationSource.toString(), rule.getDrawflowConfig());
                    // 拼装redis中信源池配置
                    InformationSourceJobConfigModel model = new InformationSourceJobConfigModel();
                    model.setJobId(jobId);
                    model.setTaskId(taskId);
                    model.setTaskName(task.getName());
                    model.setTimeout(task.getTimeout());
                    model.setCrawlId(crawlId);
                    model.setCrawlName(crawl.getName());
                    model.setCrawlType(crawl.getType());
                    model.setRepository(crawl.getRepository());
                    model.setRunCommand(crawl.getRunCommand());
                    model.setPreCommand(crawl.getPreCommand());
                    model.setBranch(crawl.getBranch());
                    model.setVersion(crawl.getVersion());
                    model.setInformationSourceId(id);
                    model.setInformationSourceName(informationSource.getName());
                    model.setDomain(informationSource.getDomain());
                    model.setScheme(informationSource.getScheme());
                    model.setPort(informationSource.getPort());
                    model.setRule(rule.getDrawflowConfig());
                    model.setIpProxyApi(ipProxyApi);
                    // 写入队列
                    if (!polymerizeRedisUtil.lPush(redisQueueKey, model, redisQueueExpire)) {
                        throw new Exception("informationSourceId=" + id + ", informationSourceName=" + informationSource.getName() + ", 加入redis [" + redisQueueKey + "]任务队列失败");
                    }
                }
            }
            log.info("Producer 任务 jobId={}, instanceId={}, 执行成功", jobId, instanceId);
            omsLogger.info("Producer 任务 jobId={}, instanceId={}, 执行成功", jobId, instanceId);
        } catch (Exception e) {
            // 删除redis已经写入的任务队列
            polymerizeRedisUtil.del(redisQueueKey);
            log.error("Producer 任务 jobId={}, instanceId={}, 执行失败, 原因: ", jobId, instanceId, e.getMessage());
            omsLogger.error("Producer 任务 jobId={}, instanceId={}, 执行失败, 原因: ", jobId, instanceId, e.getMessage());
            throw e;
        }
    }

}
