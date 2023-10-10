package org.jeecg.modules.polymerize.playwright.config;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.springframework.context.annotation.Bean;

import java.util.EnumSet;
import java.util.Set;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/9/1 10:20
 */
@org.springframework.context.annotation.Configuration
public class JsonPathConfig {

    @Bean
    public void init() {
        Configuration.Defaults conf = new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        };

        Configuration.setDefaults(conf);
    }

}
