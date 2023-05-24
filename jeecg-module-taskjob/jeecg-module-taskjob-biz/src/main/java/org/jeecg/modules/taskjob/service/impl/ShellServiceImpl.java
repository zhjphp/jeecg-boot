package org.jeecg.modules.taskjob.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.util.OSUtil;
import org.jeecg.modules.taskjob.service.IShellService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.log.OmsLogger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @version 1.0
 * @description: ShellService shell脚本相关服务
 * @author: wayne
 * @date 2023/5/23 15:48
 */
@Slf4j
@Component
public class ShellServiceImpl implements IShellService {

    /** 生成执行爬虫shell脚本的文件名 */
    @Value("${taskjob.consumer.shellFileName}")
    private String shellFileName;

    /**
     * 生成 shell 脚本文件
     *
     * @param path
     * @param command
     * @param omsLogger
     * @return
     * @throws Exception
     */
    @Override
    public synchronized String makeShellFile(String path, String command, OmsLogger omsLogger) throws Exception {
        String suffix = null;
        BufferedWriter bw = null;
        log.info("判断操作系统:");
        omsLogger.info("判断操作系统:");
        // 判断操作系统
        if (OSUtil.isWindows()) {
            log.info("当前操作系统是 windows");
            omsLogger.info("当前操作系统是 windows");
            suffix = ".ps1";
        } else if (OSUtil.isLinux()) {
            log.info("当前操作系统是 linux");
            omsLogger.info("当前操作系统是 linux");
            suffix = ".sh";
        } else {
            throw new Exception("不支持的操作系统");
        }
        String fullName = shellFileName + suffix;
        String filePath = path + File.separator + fullName;
        File file = new File(filePath);
        if (!file.exists()) {
            log.info("在目录: {}, 建立shell文件: {}", path, fullName);
            omsLogger.info("在目录: {}, 建立shell文件: {}", path, fullName);
            file.createNewFile();
        } {
            log.info("在目录: {}, shell文件: {}, 已存在, 不再建立", path, fullName);
        }
        try {
            // 写入shell文件内容
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write(command);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            bw.close();
        }
        return fullName;
    }

    /**
     * 命令行执行 shell 脚本
     *
     * @param shellFileFullName
     * @param codePath
     * @param omsLogger
     * @return
     * @throws Exception
     */
    @Override
    public void runShell(String shellFileFullName, String codePath, OmsLogger omsLogger) throws Exception {
        BufferedReader bufferedReader = null;
        try {
            int exitValue = -1;
            // shell执行程序
            String program = null;
            // 命令行输出编码
            Charset lineCharset = null;
            // 判断系统
            if (OSUtil.isWindows()) {
                program = "powershell";
                log.info("windows 系统使用 {} 执行shell文件, 输出编码为: GBK", program);
                omsLogger.info("windows 系统使用 {} 执行shell文件, 输出编码为: GBK", program);
                lineCharset = Charset.forName("GBK");
            } else if (OSUtil.isLinux()) {
                program = "bash";
                log.info("linux 系统使用 {} 执行shell文件, 输出编码为: UTF-8", program);
                omsLogger.info("linux 系统使用 {} 执行shell文件, 输出编码为: UTF-8", program);
                lineCharset = StandardCharsets.UTF_8;
            } else {
                throw new Exception("执行shell失败, 不支持的操作系统");
            }
            // 开始执行脚本
            log.info("开始执行shell: {}", shellFileFullName);
            omsLogger.info("开始执行shell: {}", shellFileFullName);
            Process process = Runtime.getRuntime().exec(program + " ./" + shellFileFullName, null, new File(codePath));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream, lineCharset));
            // command log
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.info(line);
                omsLogger.info(line);
            }
            exitValue = process.waitFor();
            if (exitValue == 0) {
                // default success
                log.info("shell执行成功, exitValue: {}", exitValue);
                omsLogger.info("shell执行成功, exitValue: {}", exitValue);
            } else {
                throw new Exception("shell执行失败, exitValue: " + exitValue);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

}
