package org.jeecg.modules.taskjob.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.model.InformationSourceJobConfigModel;
import org.jeecg.modules.polymerize.util.PolymerizeRedisUtil;
import org.jeecg.modules.taskjob.service.IConsumerService;
import org.jeecg.modules.taskjob.service.IGitService;
import org.jeecg.modules.taskjob.service.IShellService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import tech.powerjob.common.serialize.JsonUtils;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.log.OmsLogger;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
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
    private IShellService shellService;

    @Value("${taskjob.consumer.crawlBaseCodePath}")
    private String baseCodePath;

    /**
     * 执行命令预留参数位置
     * param1 将会被替换为job的json配置字符串
     */
    @Value("${taskjob.consumer.command.param1}")
    private String param1;

    /**
     * 执行命令预留参数位置
     * param2 将会被替换为IP地址池的json数组
     */
    @Value("${taskjob.consumer.command.param2}")
    private String param2;

    /**
     * 执行命令预留参数位置
     * param3 将会被替换为header池的json数组
     */
    @Value("${taskjob.consumer.command.param3}")
    private String param3;

    @Value("${taskjob.redis.informationSourceQueueKeyPre}")
    private String redisQueueKeyPre;

    @Value("${taskjob.redis.informationSourceQueueExpire}")
    private long redisQueueExpire;

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
                // shell脚本命令内容
                String command = jobConfig.getRunCommand();
                log.info("爬虫执行命令: {}", command);
                omsLogger.info("爬虫执行命令: {}", command);
                // 执行命令中包含预留参数位置将会被替换, 给爬虫传参使用base64编码
                if (command.contains(this.param1)) {
                    command = command.replace(this.param1, Base64.getEncoder().encodeToString(jobStr.getBytes("UTF-8")));
                    log.info("替换占位符: {}", command);
                    omsLogger.info("替换占位符: {}", command);
                }
//                if (command.contains(this.param2)) {
//                    command = command.replace(this.param2, Base64.getEncoder().encodeToString(jobStr.getBytes("UTF-8")));
//                    log.info("替换占位符: {}", command);
//                    omsLogger.info("替换占位符: {}", command);
//                }
//                if (command.contains(this.param2)) {
//                    command = command.replace(this.param2, Base64.getEncoder().encodeToString(jobStr.getBytes("UTF-8")));
//                    log.info("替换占位符: {}", command);
//                    omsLogger.info("替换占位符: {}", command);
//                }
                // 封装shell文件
                log.info("开始建立shell执行文件");
                omsLogger.info("开始建立shell执行文件");
                // 文件唯一标识,避免冲突
                String fileId = jobConfig.getInformationSourceId() + "_" + DigestUtils.md5DigestAsHex(command.getBytes(StandardCharsets.UTF_8));
                String shellFileFullName = shellService.makeShellFile(codePath, command, fileId, omsLogger);
                // 执行shell脚本
                shellService.runShell(shellFileFullName, codePath, omsLogger);
            }
        } catch (Exception e) {
            // 删除redis已经写入的任务队列
            polymerizeRedisUtil.del(redisQueueKey);
            throw e;
        }
    }

}
