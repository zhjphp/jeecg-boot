package org.jeecg.modules.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.polymerize.dto.TmpCrawlDataDTO;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import org.jeecg.modules.polymerize.service.ITmpCrawlDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/7/7 14:01
 */
@Api(tags="无验证API接口")
@Slf4j
@RestController
@RequestMapping("/polymerize/noAuth/api")
public class PolymerizeNoAuthApiController {

    @Autowired
    private ITmpCrawlDataService tmpCrawlDataService;

    @ApiOperation(value="爬虫临时数据存储-接口数据", notes="爬虫临时数据存储-接口数据")
    @PostMapping(value = "/queryTmpCrawlData")
    public Result<IPage<TmpCrawlData>> queryTmpCrawlData(TmpCrawlDataDTO tmpCrawlDataDTO,
                                                         @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                         @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                         HttpServletRequest req) {
        IPage<TmpCrawlData> list = tmpCrawlDataService.queryTmpCrawlData(tmpCrawlDataDTO, pageNo, pageSize);
        return Result.OK(list);
    }

}
