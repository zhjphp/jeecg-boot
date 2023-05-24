package org.jeecg.modules.taskjob.service;

import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.log.OmsLogger;

public interface IProducerService {

    void doJob(long jobId, String taskIds, long instanceId, OmsLogger omsLogger) throws Exception;

}
