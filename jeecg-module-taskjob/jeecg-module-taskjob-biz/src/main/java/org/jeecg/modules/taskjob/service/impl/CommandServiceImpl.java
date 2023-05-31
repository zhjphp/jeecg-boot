package org.jeecg.modules.taskjob.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.util.OSUtil;
import org.jeecg.modules.taskjob.service.ICommandService;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;
import tech.powerjob.worker.log.OmsLogger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @version 1.0
 * @description: 命令执行服务
 * @author: wayne
 * @date 2023/5/26 21:47
 */
@Slf4j
@Component
public class CommandServiceImpl implements ICommandService {

    /**
     * 常规流处理
     */
    public class TaskjobLogOutputStream extends LogOutputStream {

        private OmsLogger omsLogger;

        public TaskjobLogOutputStream(OmsLogger omsLogger, String outputCharset) {
            super();
            this.setOutputCharset(outputCharset);
            this.omsLogger = omsLogger;
        }

        @Override
        protected void processLine(String line) {
            log.info(line);
            omsLogger.info(line);
        }
    }

    /**
     * 错误流处理
     */
    public class TaskjobLogErrorStream extends LogOutputStream  {

        private OmsLogger omsLogger;

        public TaskjobLogErrorStream(OmsLogger omsLogger, String outputCharset) {
            super();
            this.setOutputCharset(outputCharset);
            this.omsLogger = omsLogger;
        }

        @Override
        protected void processLine(String line) {
            log.error(line);
            omsLogger.error(line);
        }
    }

    /**
     * 按逗号拆分指令参数,指令参数分隔符为逗号
     *
     * @param command
     * @return
     * @throws Exception
     */
    public List<String> splitCommand(String command) throws Exception {
        // 按逗号分割
        // String[] commandArray = command.split(",");
        // 按空格分割
        String [] commandArgumentArray = command.split("\\s+");
        ArrayList<String> commandArgumentList = new ArrayList<String>(commandArgumentArray.length);
        Collections.addAll(commandArgumentList, commandArgumentArray);
        return commandArgumentList;
    }

    /**
     * 执行 command
     *
     * @param command
     * @param workDirectory
     * @param timeout
     * @param envMap
     * @param omsLogger
     * @return
     * @throws Exception
     */
    @Override
    public int runCommand(String command, String workDirectory, long timeout, Map<String, String> envMap, OmsLogger omsLogger) throws Exception {
        int exitValue = -1;
        try {
            // 操作系统
            String osType = null;
            // 命令执行程序
            String program = null;
            // 命令行输出编码
            String outputCharset = null;
            // 判断系统
            if (OSUtil.isWindows()) {
                osType = "windows";
                program = "powershell";
                outputCharset = "GBK";
            } else if (OSUtil.isLinux()) {
                osType = "linux";
                program = "bash";
                outputCharset = "UTF-8";
            } else {
                throw new Exception("不支持的操作系统");
            }
            log.info("当前系统: {}, 输出编码: {}", osType, outputCharset);
            omsLogger.info("当前系统: {}, 输出编码: {}", osType, outputCharset);
            // 执行器
            ProcessExecutor processExecutor = new ProcessExecutor();
            // 命令参数列表
            processExecutor.commandSplit(command);
            log.info("执行命令: {}", processExecutor.getCommand());
            omsLogger.info("执行命令: {}", processExecutor.getCommand());
            // 设置工作目录
            if (oConvertUtils.isNotEmpty(workDirectory)) {
                processExecutor.directory(new File(workDirectory));
            }
            // 设置环境变量
            if (envMap != null && envMap.size() > 0) {
                processExecutor.environment(envMap);
            }
            // 执行超时时间
            if ( timeout > 0 ) {
                processExecutor.timeout(timeout, TimeUnit.SECONDS);
            }
            // 输出日志
            processExecutor.readOutput(true);
            processExecutor.redirectOutput(new TaskjobLogOutputStream(omsLogger, "GBK"));
            processExecutor.redirectError(new TaskjobLogErrorStream(omsLogger, "GBK"));
            // 执行命令
            try {
                exitValue = processExecutor.destroyOnExit().execute().getExitValue();
            } catch (TimeoutException e) {
                log.error("命令执行超时: {}", e.getMessage());
                omsLogger.error("命令执行超时: {}", e.getMessage());
                throw e;
            }

            return exitValue;
        } catch (Exception e) {
            log.error("runCommand 异常");
            omsLogger.error("runCommand 异常");
            throw e;
        }
    }

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
    @Override
    public synchronized void runPreCommand(String preCommand, String workDirectory, long timeout, OmsLogger omsLogger) throws Exception {
        int exitValue = -1;
        try {
            // 处理预处理指令
            // 按换行符拆分命令为多条
            List<String> preCommandList = Arrays.asList(preCommand.split("\n"));
            // 遍历执行预处理指令
            for (String command: preCommandList) {
                exitValue = runCommand(command, workDirectory, timeout, null, omsLogger);
                if (exitValue != 0) {
                    log.error("预处理指令: {}, 执行失败, exitValue={}", command, exitValue);
                    omsLogger.error("预处理指令: {}, 执行失败, exitValue={}", command, exitValue);
                    throw new Exception("预处理指令: " + command + ", 执行失败, exitValue=" + exitValue);
                } else {
                    log.info("预处理指令: {}, 执行成功, exitValue={}", command, exitValue);
                    omsLogger.info("预处理指令: {}, 执行成功, exitValue={}", command, exitValue);
                }
            }
            log.info("全部预处理指令, 执行完成");
            omsLogger.info("全部预处理指令, 执行完成");
        } catch (Exception e) {
            log.error("预处理指令, 执行失败, exitValue={}", exitValue);
            omsLogger.error("预处理指令, 执行失败, exitValue={}", exitValue);
            throw e;
        }
    }

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
    @Override
    public void runCrawlCommand(String preCommand, String workDirectory, long timeout, OmsLogger omsLogger) throws Exception {
        int exitValue = -1;
        try {
            // 处理预处理指令
            // 按换行符拆分命令为多条
            List<String> preCommadList = Arrays.asList(preCommand.split("\n"));
            // 遍历执行预处理指令
            for (String command: preCommadList) {
                exitValue = runCommand(command, workDirectory, timeout, null, omsLogger);
                if (exitValue != 0) {
                    log.error("爬虫指令: {}, 执行失败, exitValue={}", command, exitValue);
                    omsLogger.error("爬虫指令: {}, 执行失败, exitValue={}", command, exitValue);
                    throw new Exception("爬虫指令: " + command + ", 执行失败, exitValue=" + exitValue);
                } else {
                    log.info("爬虫指令: {}, 执行成功, exitValue={}", command, exitValue);
                    omsLogger.info("爬虫指令: {}, 执行成功, exitValue={}", command, exitValue);
                }
            }
            log.info("全部爬虫指令, 执行完成");
            omsLogger.info("全部爬虫指令, 执行完成");
        } catch (Exception e) {
            log.error("爬虫指令, 执行失败, exitValue={}", exitValue);
            omsLogger.error("爬虫指令, 执行失败, exitValue={}", exitValue);
            throw e;
        }
    }

}
