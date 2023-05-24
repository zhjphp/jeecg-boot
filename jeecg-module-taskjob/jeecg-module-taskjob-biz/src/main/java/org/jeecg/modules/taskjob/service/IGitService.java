package org.jeecg.modules.taskjob.service;

import tech.powerjob.worker.log.OmsLogger;

public interface IGitService {

    /**
     * 从 git 拉取代码
     *
     * @param codePath
     * @param crawlRepository
     * @param omsLogger
     * @return
     * @throws Exception
     */
    void gitClone(String codePath, String crawlRepository, OmsLogger omsLogger) throws Exception;

    /**
     * 获取git仓库名称
     *
     * @param gitUrl
     * @return String
     * @throws Exception
     */
    String getGitRepositoryName(String gitUrl) throws Exception;

}
