package org.jeecg.modules.polymerize.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.entity.PolymerizeCategory;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import org.jeecg.modules.polymerize.mapper.TmpCrawlDataMapper;
import org.jeecg.modules.polymerize.service.ITmpCrawlDataService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * @Description: 爬虫临时数据存储
 * @Author: jeecg-boot
 * @Date:   2023-06-30
 * @Version: V1.0
 */
@Slf4j
@Service
public class TmpCrawlDataServiceImpl extends ServiceImpl<TmpCrawlDataMapper, TmpCrawlData> implements ITmpCrawlDataService {

    @Resource
    TmpCrawlDataMapper tmpCrawlDataMapper;

    @Override
    public boolean addTmpCrawlData(TmpCrawlData tmpCrawlData) {
        // 判断数据库中是否已经存在当前数据
        LambdaQueryWrapper<TmpCrawlData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TmpCrawlData::getUrl, tmpCrawlData.getUrl());
        long count = tmpCrawlDataMapper.selectCount(queryWrapper);
        log.info("addTmpCrawlData-url: {}, count: {}", tmpCrawlData.getUrl(), count);
        if ( count >= 1) {
            log.info("重复数据,不执行插入");
            return true;
        }
        log.info("无重复数据,执行插入");
        log.info("tmpCrawlData: {}", tmpCrawlData.toString());
        int result = tmpCrawlDataMapper.insert(tmpCrawlData);
        if (result == 1) {
            return true;
        }
        return false;
    }

}
