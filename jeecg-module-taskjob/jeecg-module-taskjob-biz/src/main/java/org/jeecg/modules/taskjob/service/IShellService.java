package org.jeecg.modules.taskjob.service;

import tech.powerjob.worker.log.OmsLogger;

public interface IShellService {

    /**
     * 生成 shell 脚本文件
     *
     * @param path
     * @param command
     * @param omsLogger
     * @return
     * @throws Exception
     */
    String makeShellFile(String path, String command, OmsLogger omsLogger) throws Exception;

    /**
     * 命令行执行 shell 脚本
     *
     * @param shellFileFullName
     * @param codePath
     * @param omsLogger
     * @return
     * @throws Exception
     */
    void runShell(String shellFileFullName, String codePath, OmsLogger omsLogger) throws Exception;

}
