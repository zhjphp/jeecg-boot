package org.jeecg.modules.proxy.api;
import org.jeecg.modules.proxy.api.fallback.ProxyHelloFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "jeecg-proxy", fallbackFactory = ProxyHelloFallback.class)
public interface ProxyHelloApi {

    /**
     * proxy hello 微服务接口
     * @param
     * @return
     */
    @GetMapping(value = "/proxy/hello")
    String callHello();
}
