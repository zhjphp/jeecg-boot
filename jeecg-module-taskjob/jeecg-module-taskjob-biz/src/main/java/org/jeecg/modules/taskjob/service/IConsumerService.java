package org.jeecg.modules.taskjob.service;

import tech.powerjob.worker.log.OmsLogger;

/**
 * @version 1.0
 * @description: ConsumerService
 * @author: wayne
 * @date 2023/5/23 14:06
 */
public interface IConsumerService {

    /**
     * 执行Consumer任务
     *
     * @param jobId
     * @param instanceId
     * @param omsLogger
     * @return
     * @throws Exception
     */
    void doJob(long jobId, long instanceId, OmsLogger omsLogger) throws Exception;

}
