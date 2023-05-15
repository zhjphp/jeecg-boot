package org.jeecg.modules.polymerize.api;

import org.jeecg.common.constant.ServiceNameConstants;
import org.jeecg.modules.polymerize.api.factory.PolymerizeAPIFallbackFactory;
import org.jeecg.modules.polymerize.entity.Crawl;
import org.jeecg.modules.polymerize.entity.InformationSource;
import org.jeecg.modules.polymerize.entity.InformationSourceRule;
import org.jeecg.modules.polymerize.entity.InformationSourceTask;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Component
@FeignClient(contextId = "polymerizeRemoteApi", value = ServiceNameConstants.SERVICE_SYSTEM, fallbackFactory = PolymerizeAPIFallbackFactory.class)
public interface IPolymerizeAPI {

    @PostMapping("/test")
    void test();

    @GetMapping("/polymerize/api/queryTaskById")
    InformationSourceTask queryTaskById(@RequestParam("id") String id);

    @GetMapping("/polymerize/api/queryCrawById")
    Crawl queryCrawlById(@RequestParam("id") String id);

    @GetMapping("/polymerize/api/queryInformationSourceById")
    InformationSource queryInformationSourceById(@RequestParam("id") String id);

    @GetMapping("/polymerize/api/queryRuleByInformationSourceId")
    InformationSourceRule queryRuleByInformationSourceId(@RequestParam("id") String id);

}
