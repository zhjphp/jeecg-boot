package org.jeecg.modules.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.entity.*;
import org.jeecg.modules.polymerize.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @description: Polymerize API
 * @author: wayne
 * @date 2023/5/11 22:07
 */
@Slf4j
@RestController
@RequestMapping("/polymerize/api")
public class PolymerizeApiController {

    @Resource
    IInformationSourceTaskService informationSourceTaskService;

    @Resource
    ICrawlService crawlService;

    @Resource
    IInformationSourceService informationSourceService;

    @Resource
    IInformationSourceRuleService informationSourceRuleService;

    @Autowired
    private ITmpCrawlDataService tmpCrawlDataService;

    /**
     * 通过 id 查询 task
     * @param id
     * @return
     */
    @GetMapping("/queryTaskById")
    public InformationSourceTask queryTaskById(@RequestParam("id") String id) {
        return informationSourceTaskService.getById(id);
    }

    /**
     * 通过 id 查询 crawl
     * @param id
     * @return
     */
    @GetMapping("/queryCrawById")
    public Crawl queryCrawlById(@RequestParam("id") String id) {
        return crawlService.getById(id);
    }

    /**
     * 通过 id 查询 informationSourceService
     * @param id
     * @return
     */
    @GetMapping("/queryInformationSourceById")
    public InformationSource queryInformationSourceById(@RequestParam("id") String id) {
        return informationSourceService.getById(id);
    }

    /**
     * 通过 id 查询 informationSourceService
     * @param id
     * @return
     */
    @GetMapping("/queryRuleByInformationSourceId")
    public InformationSourceRule queryRuleByInformationSourceId(@RequestParam("id") String id) {
        return informationSourceRuleService.getOne(new LambdaQueryWrapper<InformationSourceRule>().eq(InformationSourceRule::getInformationSourceId, id));
    }

    @PostMapping("/addTmpCrawlData")
    public boolean addTmpCrawlData(@RequestBody TmpCrawlData tmpCrawlData) {
        return tmpCrawlDataService.addTmpCrawlData(tmpCrawlData);
    }

}
