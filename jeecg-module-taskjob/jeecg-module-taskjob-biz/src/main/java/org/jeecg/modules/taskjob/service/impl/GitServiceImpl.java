package org.jeecg.modules.taskjob.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jeecg.modules.taskjob.service.IGitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.log.OmsLogger;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @description: GitService git仓库相关服务
 * @author: wayne
 * @date 2023/5/23 15:28
 */
@Slf4j
@RefreshScope
@Component
public class GitServiceImpl implements IGitService {

    @Value("${taskjob.consumer.git.username}")
    private String userName;

    @Value("${taskjob.consumer.git.password}")
    private String password;

    /**
     * 从 git 拉取代码
     *
     * @param codePath
     * @param crawlRepository
     * @param omsLogger
     * @return
     * @throws Exception
     */
    @Override
    public synchronized void gitClone(String codePath, String crawlRepository, OmsLogger omsLogger) throws Exception {
        // 配置git仓库
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(userName, password);
        // 判断爬虫代码是否存在
        File folder = new File(codePath);
        if (!folder.exists()) {
            boolean mkResult = folder.mkdirs();
            log.info("爬虫代码目录不存在, 创建目录: {}", mkResult);
            omsLogger.info("爬虫代码目录不存在, 创建目录: {}", mkResult);
        }
        if (folder.list().length == 0) {
            log.info("爬虫代码目录为空目录, 从 {} 下载代码", crawlRepository);
            omsLogger.info("爬虫代码目录为空目录, 从 {} 下载代码", crawlRepository);
            Git cloneGit = Git.cloneRepository().setURI(crawlRepository).setDirectory(folder).setCredentialsProvider(provider).call();
            cloneGit.close();
        } else {
            log.info("爬虫代码已经存在, 不执行下载");
            omsLogger.info("爬虫代码已经存在, 不执行下载");
        }
    }

    /**
     * 获取git仓库名称
     *
     * @param gitUrl
     * @return String
     * @throws Exception
     */
    @Override
    public String getGitRepositoryName(String gitUrl) throws Exception {
        String regEx = "(.+/)(.+)(\\.git)$";
        int group = 2;
        Matcher matcher = Pattern.compile(regEx).matcher(gitUrl);
        matcher.matches();
        return matcher.group(group);
    }

}
