package org.jeecg.modules.polymerize.api.fallback;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.api.IPolymerizeAPI;
import org.jeecg.modules.polymerize.entity.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @version 1.0
 * @description: PolymerizeAPIFallback
 * @author: wayne
 * @date 2023/5/11 13:59
 */
@Slf4j
public class PolymerizeAPIFallback implements IPolymerizeAPI {

    @Setter
    Throwable cause;

    @Override
    public void test() {
        log.error("测试失败");
    }

    @Override
    public InformationSourceTask queryTaskById(String id) {
        log.error("queryTaskById 请求失败");
        return null;
    }

    @Override
    public Crawl queryCrawlById(@RequestParam("id") String id) {
        log.error("queryCrawlById 请求失败");
        return null;
    }

    @Override
    public InformationSource queryInformationSourceById(@RequestParam("id") String id) {
        log.error("queryInformationSourceById 请求失败");
        return null;
    }

    @Override
    public InformationSourceRule queryRuleByInformationSourceId(@RequestParam("id") String id) {
        log.error("queryRuleByInformationSourceId 请求失败");
        return null;
    }

    @Override
    public boolean addTmpCrawlData(@RequestBody TmpCrawlData tmpCrawlData) {
        log.error("addTmpCrawlData 请求失败");
        return false;
    }

    @Override
    public boolean addTmpCrawlDataForDzw(@RequestBody TmpCrawlData tmpCrawlData) {
        log.error("addTmpCrawlData 请求失败");
        return false;
    }
}
