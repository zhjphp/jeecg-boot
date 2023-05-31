package org.jeecg.modules.taskjob.service;

import tech.powerjob.worker.log.OmsLogger;

import java.util.Map;

public interface ICommandService {

    /**
     * 执行 command line
     *
     * @param commandArray
     * @param workDirectory
     * @param timeout
     * @param envMap
     * @param omsLogger
     * @return
     * @throws Exception
     */
    int runCommand(String commandArray, String workDirectory, long timeout, Map<String, String> envMap, OmsLogger omsLogger) throws Exception;

    /**
     * 执行预处理指令
     *
     * @param preCommand
     * @param workDirectory
     * @param timeout
     * @param omsLogger
     * @return
     * @throws Exception
     */
    void runPreCommand(String preCommand, String workDirectory, long timeout, OmsLogger omsLogger) throws Exception;

    /**
     * 执行爬虫指令
     *
     * @param preCommand
     * @param workDirectory
     * @param timeout
     * @param omsLogger
     * @return
     * @throws Exception
     */
    void runCrawlCommand(String preCommand, String workDirectory, long timeout, OmsLogger omsLogger) throws Exception;

}
