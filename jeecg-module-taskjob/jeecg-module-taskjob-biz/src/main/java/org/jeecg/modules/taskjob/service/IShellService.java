package org.jeecg.modules.taskjob.service;

import tech.powerjob.worker.log.OmsLogger;

public interface IShellService {

    String makeShellFile(String path, String command, OmsLogger omsLogger) throws Exception;

    void runShell(String shellFileFullName, String codePath, OmsLogger omsLogger) throws Exception;

}
