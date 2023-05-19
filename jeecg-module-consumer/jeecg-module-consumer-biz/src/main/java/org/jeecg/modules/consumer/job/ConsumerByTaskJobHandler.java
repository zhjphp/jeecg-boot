package org.jeecg.modules.consumer.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.constant.PolymerizeCacheConstant;
import org.jeecg.modules.polymerize.model.InformationSourceJobConfigModel;
import org.jeecg.modules.polymerize.util.OSUtil;
import org.jeecg.modules.polymerize.util.PolymerizeRedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @description: 信源任务执行器
 * @author: wayne
 * @date 2023/5/15 21:32
 */
@Slf4j
@Component
public class ConsumerByTaskJobHandler {

    @Resource
    private PolymerizeRedisUtil polymerizeRedisUtil;

    // 爬虫存储目录
    @Value("${consumer.crawlBaseCodePath}")
    private String baseCodePath;

    // git仓库用户名
    @Value("${consumer.git.username}")
    private String userName;

    // git仓库密码
    @Value("${consumer.git.password}")
    private String password;

    // 生成执行爬虫shell脚本的文件名
    @Value("${consumer.shellFileName}")
    private String shellFileName;

    /**
     * 执行命令预留参数位置
     * param1 将会被替换为 job 的 json 配置字符串
     * */
    @Value("${consumer.command.param1}")
    private String param1;

    @XxlJob(value = "consumerByTaskJobHandler")
    public void consumerByTaskJobHandler() throws Exception {
        // 当前执行的jobId
        long jobId = XxlJobHelper.getJobId();
        String jobIdStr = String.valueOf(jobId);

        try {
            // 任务参数传递生产者任务ID
            String producerId = XxlJobHelper.getJobParam();
            if (oConvertUtils.isEmpty(producerId)) {
                throw new Exception("生产者ID为空");
            }
            // redis 任务队列key
            String redisKey = PolymerizeCacheConstant.INFORMATION_SOURCE_JOB_LIST_PRE + ":" + producerId;
            // 循环从队列中取出信源配置
            addLog("开始从任务队列: " + redisKey + ", 循环取出任务");
            Object jobObject;
            while ( (jobObject = polymerizeRedisUtil.lPop(redisKey)) != null ) {
                String jobStr = JSONObject.toJSONString(jobObject);
                // JSON.parseObject(jobStr, InformationSourceJobConfigModel.class);
                InformationSourceJobConfigModel jobConfig = (InformationSourceJobConfigModel)jobObject;
                addLog("取出任务jobConfig: " + jobConfig.toString());

                // 获取任务爬虫存储路径
                addLog("从 " + jobConfig.getRepository() + " 匹配仓库名称");
                String gitRepositoryName = getGitRepositoryName(jobConfig.getRepository());

                // 代码存储地址为: 基础路径 + 仓库名称 + 版本号
                String codePath = baseCodePath + gitRepositoryName + File.separator + jobConfig.getVersion();
                addLog("爬虫存储路径: " + codePath);

                // 从git拉取爬虫代码
                gitClone(codePath, jobConfig.getRepository());

                // shell脚本命令内容
                String command = jobConfig.getRunCommand();
                addLog("爬虫执行命令: " + command);
                // 执行命令中包含预留参数位置将会被替换, 给爬虫传参使用base64编码
                if (command.contains(this.param1)) {
                    command = command.replace(this.param1, Base64.getEncoder().encodeToString(jobStr.getBytes("UTF-8")));
                    addLog("替换占位符: " + command);
                } else {
                    throw new Exception("执行命令没有预留信源规则配置传入参数");
                }

                // 封装shell文件
                addLog("建立shell执行文件");
                String shellFileFullName = makeShellFile(codePath, command);

                // 执行shell脚本
                runShell(shellFileFullName, codePath);
            }

            log.info("job jobId=" + jobId + ", 执行完成");
            XxlJobHelper.handleSuccess("job jobId=" + jobId + ", 执行完成");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            XxlJobHelper.handleFail( e.getMessage());
            return;
        }
    }

    /**
     * 从 git 拉取代码
     *
     * @param codePath
     * @param crawlRepository
     * @return
     */
    private synchronized void gitClone(String codePath, String crawlRepository) throws Exception {
        // 配置git仓库
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(userName, password);
        // 判断爬虫代码是否存在
        File folder = new File(codePath);
        if (!folder.exists()) {
            boolean mkResult = folder.mkdirs();
            addLog("爬虫代码目录不存在, 创建目录: " + mkResult);
        }
        if (folder.list().length == 0) {
            addLog("爬虫代码目录为空目录, 从: " + crawlRepository + " 下载代码");
            Git cloneGit = Git.cloneRepository().setURI(crawlRepository).setDirectory(folder).setCredentialsProvider(provider).call();
            TimeUnit.SECONDS.sleep(7);
            cloneGit.close();
        } else {
            addLog("爬虫代码已经存在, 不执行下载");
        }
    }

    /**
     * 命令行执行 shell 脚本
     *
     * @param shellFileFullName
     * @param codePath
     * @return
     */
    private void runShell(String shellFileFullName, String codePath) throws Exception {
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
                lineCharset = Charset.forName("GBK");
            } else if (OSUtil.isLinux()) {
                program = "bash";
                lineCharset = StandardCharsets.UTF_8;
            } else {
                throw new Exception("不支持的操作系统");
            }
            // 开始执行脚本
            addLog("开始执行shell脚本: " + shellFileFullName);
            Process process = Runtime.getRuntime().exec(program + " ./" + shellFileFullName, null, new File(codePath));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream, lineCharset));
            // command log
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                addLog(line);
            }
            exitValue = process.waitFor();
            if (exitValue == 0) {
                // default success
                addLog("shell脚本执行完成, exitValue: " + exitValue);
            } else {
                throw new Exception("command exit value("+exitValue+") is failed");
            }
            addLog("shell status: " + exitValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    /**
     * 生成 shell 脚本文件
     *
     * @param path
     * @param command
     * @return
     */
    private synchronized String makeShellFile(String path, String command) throws Exception {
        String suffix = null;
        BufferedWriter bw = null;
        // 判断操作系统
        if (OSUtil.isWindows()) {
            addLog("当前操作系统是 windows");
            suffix = ".ps1";
        } else if (OSUtil.isLinux()) {
            addLog("当前操作系统是 linux");
            suffix = ".sh";
        } else {
            throw new Exception("不支持的操作系统");
        }
        String fullName = shellFileName + suffix;
        String filePath = path + File.separator + fullName;
        File file = new File(filePath);
        if (!file.exists()) {
            addLog("在目录: " + path + " , 建立shell文件: " + fullName);
            file.createNewFile();
        } {
            addLog("在目录: " + path + " , shell文件: " + fullName + " 已存在,不再建立");
        }
        try {
            // 写入shell文件内容
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write(command);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            bw.close();
        }
        return fullName;
    }

    /**
     * 获取git仓库名称
     *
     * @param gitUrl
     * @return
     */
    private String getGitRepositoryName(String gitUrl) throws Exception {
        try {
            String regEx = "(.+/)(.+)(\\.git)$";
            int group = 2;
            Matcher matcher = Pattern.compile(regEx).matcher(gitUrl);
            matcher.matches();
            return matcher.group(group);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("git仓库名称匹配失败, " + e.getMessage());
        }
    }

    /**
     * 打印日志
     *
     * @param msg
     * @return
     */
    private void addLog(String msg) {
        log.info(msg);
        XxlJobHelper.log(msg);
    }

}
