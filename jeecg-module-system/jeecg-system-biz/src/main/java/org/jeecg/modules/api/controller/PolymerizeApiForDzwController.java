package org.jeecg.modules.api.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.api.controller.dataclean.DataCleaningUtil;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import org.jeecg.modules.polymerize.service.ITmpCrawlDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @description: Polymerize API For Dzw
 * @author: wyx
 * @date 2023/10/19 16:07
 */
@Slf4j
@RestController
@RequestMapping("/polymerize/dzw/api")
public class PolymerizeApiForDzwController {

    @Resource
    private ITmpCrawlDataService tmpCrawlDataService;

    @DS("xzsd")
    @PostMapping("/addTmpCrawlDataForDzw")
    public boolean addTmpCrawlDataForDzw(@RequestBody TmpCrawlData tmpCrawlData) {
        //数据处理逻辑测试-待添加
        DataCleaningUtil.tmpCrawlDataCleaningForDzw(tmpCrawlData);
        return tmpCrawlDataService.addTmpCrawlData(tmpCrawlData);
    }
}
