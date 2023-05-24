package org.jeecg.modules.taskjob.service;

import tech.powerjob.worker.log.OmsLogger;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/5/23 14:06
 */
public interface IConsumerService {

    void doJob(long jobId, long instanceId, OmsLogger omsLogger) throws Exception;

}
