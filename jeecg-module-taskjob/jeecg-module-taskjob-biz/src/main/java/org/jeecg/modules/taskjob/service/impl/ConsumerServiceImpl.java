package org.jeecg.modules.taskjob.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.entity.Crawl;
import org.jeecg.modules.polymerize.model.InformationSourceJobConfigModel;
import org.jeecg.modules.polymerize.playwright.PlaywrightCrawl;
import org.jeecg.modules.polymerize.util.PolymerizeRedisUtil;
import org.jeecg.modules.taskjob.service.ICommandService;
import org.jeecg.modules.taskjob.service.IConsumerService;
import org.jeecg.modules.taskjob.service.IGitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.log.OmsLogger;

import javax.annotation.Resource;
import java.io.File;
import java.util.Base64;

/**
 * @version 1.0
 * @description: ConsumerService 消费者服务,从信源队列中读取任务,并调用爬虫执行任务
 * @author: wayne
 * @date 2023/5/23 14:07
 */
@Slf4j
@Component
public class ConsumerServiceImpl implements IConsumerService {

    @Resource
    private PolymerizeRedisUtil polymerizeRedisUtil;

    @Resource
    private IGitService gitService;

    @Resource
    private ICommandService commandService;

    @Value("${taskjob.consumer.crawlBaseCodePath}")
    private String baseCodePath;

    @Value("${taskjob.redis.informationSourceQueueKeyPre}")
    private String redisQueueKeyPre;

    @Value("${taskjob.redis.informationSourceQueueExpire}")
    private long redisQueueExpire;

    @Value("${taskjob.consumer.command.placeholder.rule}")
    private String placeholderRule;

    @Value("${taskjob.consumer.command.placeholder.ipProxyApi}")
    private String placeholderIPProxyApi;

    /**
     * 执行Consumer任务
     *
     * @param jobId
     * @param instanceId
     * @param omsLogger
     * @return
     * @throws Exception
     */
    @Override
    public void doJob(long jobId, long instanceId, OmsLogger omsLogger) throws Exception {
        // redis信源队列key
        String redisQueueKey = redisQueueKeyPre + ":" + String.valueOf(instanceId);
        try {
            // 循环从队列中取出信源配置
            Object jobObject;
            log.info("开始从任务队列: {}, 循环取出任务", redisQueueKey);
            omsLogger.info("开始从任务队列: {}, 循环取出任务", redisQueueKey);
            while ( (jobObject = polymerizeRedisUtil.lPop(redisQueueKey)) != null ) {
                // pop出一条任务
                String jobStr = JSONObject.toJSONString(jobObject);
                InformationSourceJobConfigModel jobConfig = (InformationSourceJobConfigModel)jobObject;
                log.info("pop取出任务: {}", jobConfig.toString());
                omsLogger.info("pop取出任务: {}", jobConfig.toString());

                // 判断任务爬虫类型
                if (jobConfig.getCrawlType() == Crawl.JAVA_PLAYWRIGHT_INTERNAL) {
                    // java内置爬虫直接执行内置类逻辑
                    PlaywrightCrawl playwrightCrawl = SpringContextUtils.getApplicationContext().getBean(PlaywrightCrawl.class);
                    playwrightCrawl.run(jobConfig.getRule(), jobConfig.getInformationSourceId(), jobConfig.getTaskId(), String.valueOf(jobConfig.getJobId()));
                } else if (jobConfig.getCrawlType() == Crawl.PYTHON_ALONE || jobConfig.getCrawlType() == Crawl.PHP_ALONE) {
                    // 其他独立爬虫,通过下载仓库代码后,命令行调用执行
                    // 获取任务爬虫存储路径
                    log.info("从 {} 匹配仓库名", jobConfig.getRepository());
                    omsLogger.info("从 {} 匹配仓库名", jobConfig.getRepository());
                    String gitRepositoryName = gitService.getGitRepositoryName(jobConfig.getRepository());
                    // 代码存储地址为: 基础路径 + 仓库名称 + 版本号
                    String codePath = baseCodePath + gitRepositoryName + File.separator + jobConfig.getVersion();
                    log.info("爬虫存储路径: {}", codePath);
                    omsLogger.info("爬虫存储路径: {}", codePath);
                    // 从git拉取爬虫代码
                    gitService.gitClone(codePath, jobConfig.getRepository(), omsLogger);
                    // 处理爬虫命令内容
                    String command = jobConfig.getRunCommand();
//                log.info("爬虫执行命令: {}", command);
//                omsLogger.info("爬虫执行命令: {}", command);
                    // 执行命令中包含预留参数位置将会被替换, 给爬虫传参使用base64编码
                    // 信源规则
                    if (command.contains(placeholderRule)) {
                        command = command.replace(placeholderRule, Base64.getEncoder().encodeToString(jobStr.getBytes("UTF-8")));
                    }
                    // IP地址池
                    if (command.contains(placeholderIPProxyApi)) {
                        command = command.replace(placeholderIPProxyApi, Base64.getEncoder().encodeToString(jobConfig.getIpProxyApi().getBytes("UTF-8")));
                    }
                    if (oConvertUtils.isNotEmpty(jobConfig.getPreCommand())) {
                        // 执行预处理指令
                        commandService.runPreCommand(jobConfig.getPreCommand(), codePath, jobConfig.getTimeout(), omsLogger);
                    }
                    // 执行爬虫指令
                    commandService.runCrawlCommand(command, codePath, jobConfig.getTimeout(), omsLogger);
                }
            }
        } catch (Exception e) {
            // 删除redis已经写入的任务队列
            polymerizeRedisUtil.del(redisQueueKey);
            throw e;
        }
    }

}
