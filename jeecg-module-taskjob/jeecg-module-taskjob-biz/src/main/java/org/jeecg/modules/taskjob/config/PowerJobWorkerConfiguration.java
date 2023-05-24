package org.jeecg.modules.taskjob.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.powerjob.worker.PowerJobSpringWorker;
import tech.powerjob.worker.common.PowerJobWorkerConfig;
import tech.powerjob.worker.common.constants.StoreStrategy;

import java.util.Arrays;

/**
 * @version 1.0
 * @description: PowerJobWorkerConfiguration
 * @author: wayne
 * @date 2023/5/22 15:18
 */
@Configuration
public class PowerJobWorkerConfiguration {

    @Value("${powerjob.worker.akka-port}")
    private int akkaPort;

    @Value("${powerjob.worker.app-name}")
    private String appName;

    @Value("${powerjob.worker.server-address}")
    private String serverAddress;

    @Value("${powerjob.worker.max-result-length}")
    private int maxResultLength;

    @Value("${powerjob.worker.max-appended-wf-context-length}")
    private int maxAppendedWfContextLength;

    @Value("${powerjob.worker.max-lightweight-task-num}")
    private int maxLightweightTaskNum;

    @Value("${powerjob.worker.max-heavy-task-num}")
    private int maxHeavyUaskNum;

    @Value("${powerjob.worker.store-strategy}")
    private String storeStrategy;

    @Bean
    public PowerJobSpringWorker initPowerJobWorker() throws Exception {
        // 1. 创建配置文件
        PowerJobWorkerConfig config = new PowerJobWorkerConfig();
        config.setPort(akkaPort);
        config.setAppName(appName);
        config.setServerAddress(Arrays.asList(serverAddress.split(",")));
        // 如果没有大型 Map/MapReduce 的需求，建议使用内存来加速计算
        if (storeStrategy.equals(StoreStrategy.MEMORY.toString().toLowerCase())) {
            config.setStoreStrategy(StoreStrategy.MEMORY);
        } else if (storeStrategy.equals(StoreStrategy.DISK.toString().toLowerCase())) {
            config.setStoreStrategy(StoreStrategy.DISK);
        } else {
            config.setStoreStrategy(StoreStrategy.DISK);
        }
        config.setMaxResultLength(maxResultLength);
        config.setMaxAppendedWfContextLength(maxAppendedWfContextLength);
        config.setMaxLightweightTaskNum(maxLightweightTaskNum);
        config.setMaxHeavyweightTaskNum(maxHeavyUaskNum);

        // 2. 创建 Worker 对象，设置配置文件（注意 Spring 用户需要使用 PowerJobSpringWorker，而不是 PowerJobWorker）
        PowerJobSpringWorker worker = new PowerJobSpringWorker(config);
        return worker;
    }

}
