package org.jeecg.modules.taskjob.service;

import tech.powerjob.worker.log.OmsLogger;

public interface IGitService {

    void gitClone(String codePath, String crawlRepository, OmsLogger omsLogger) throws Exception;

    String getGitRepositoryName(String gitUrl) throws Exception;

}
