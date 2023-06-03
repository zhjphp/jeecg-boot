package org.jeecg.modules.proxy.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0
 * @description: OKHttpConfig
 * @author: wayne
 * @date 2023/6/2 10:30
 */
@Configuration
public class OKHttpConfig {

    @Bean
    public OkHttpClient getOkHttpClient() {
        return new OkHttpClient();
    }

}
