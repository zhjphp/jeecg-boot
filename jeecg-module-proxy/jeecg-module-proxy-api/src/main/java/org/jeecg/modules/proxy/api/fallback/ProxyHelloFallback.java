package org.jeecg.modules.proxy.api.fallback;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.jeecg.modules.proxy.api.ProxyHelloApi;
import lombok.Setter;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author JeecgBoot
 */
@Slf4j
@Component
public class ProxyHelloFallback implements FallbackFactory<ProxyHelloApi> {
    @Setter
    private Throwable cause;

    @Override
    public ProxyHelloApi create(Throwable throwable) {
        log.error("微服务接口调用失败： {}", cause);
        return null;
    }

}
