package org.jeecg.modules.polymerize.api.fallback;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.jeecg.modules.polymerize.api.PolymerizeHelloApi;
import lombok.Setter;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author JeecgBoot
 */
@Slf4j
@Component
public class PolymerizeHelloFallback implements FallbackFactory<PolymerizeHelloApi> {
    @Setter
    private Throwable cause;

    @Override
    public PolymerizeHelloApi create(Throwable throwable) {
        log.error("微服务接口调用失败： {}", cause);
        return null;
    }

}
