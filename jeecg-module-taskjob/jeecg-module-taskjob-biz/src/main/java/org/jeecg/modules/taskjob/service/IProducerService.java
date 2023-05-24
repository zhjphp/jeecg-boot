package org.jeecg.modules.taskjob.service;

import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.log.OmsLogger;

public interface IProducerService {

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
    void doJob(long jobId, String taskIds, long instanceId, OmsLogger omsLogger) throws Exception;

}
