package org.jeecg.modules.polymerize.api;
import org.jeecg.modules.polymerize.api.fallback.PolymerizeHelloFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "jeecg-polymerize", fallbackFactory = PolymerizeHelloFallback.class)
public interface PolymerizeHelloApi {

    /**
     * polymerize hello 微服务接口
     * @param
     * @return
     */
    @GetMapping(value = "/polymerize/hello")
    String callHello();
}
