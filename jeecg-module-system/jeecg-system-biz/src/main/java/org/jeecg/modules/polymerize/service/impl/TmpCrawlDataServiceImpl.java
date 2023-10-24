package org.jeecg.modules.polymerize.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.dto.TmpCrawlDataDTO;
import org.jeecg.modules.polymerize.entity.PolymerizeCategory;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import org.jeecg.modules.polymerize.mapper.TmpCrawlDataMapper;
import org.jeecg.modules.polymerize.service.ITmpCrawlDataService;
import org.jeecg.modules.polymerize.vo.InformationSourceVO;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public IPage<TmpCrawlData> queryTmpCrawlData(TmpCrawlDataDTO tmpCrawlDataDTO, Integer pageNo, Integer pageSize) {
        Page<TmpCrawlData> page = new Page<TmpCrawlData>(pageNo, pageSize);
        List<String> customTagList = null;
        if (oConvertUtils.isNotEmpty(tmpCrawlDataDTO.getCustomTags())) {
            customTagList = Arrays.stream(tmpCrawlDataDTO.getCustomTags().split(",")).collect(Collectors.toList());
        }

        List<String> cityList = null;
        if (oConvertUtils.isNotEmpty(tmpCrawlDataDTO.getCity())) {
            cityList = Arrays.stream(tmpCrawlDataDTO.getCity().split(",")).collect(Collectors.toList());
        }

        IPage<TmpCrawlData> pageList = tmpCrawlDataMapper.queryTmpCrawlData(
                page,
                tmpCrawlDataDTO.getInformationSourceName(),
                tmpCrawlDataDTO.getInformationSourceDomain(),
                tmpCrawlDataDTO.getTopic(),
                customTagList,
                tmpCrawlDataDTO.getInformationsourceid(),
                tmpCrawlDataDTO.getTaskid(),
                tmpCrawlDataDTO.getErrorCode(),
                tmpCrawlDataDTO.getStartCreateTime(),
                tmpCrawlDataDTO.getEndCreateTime(),
                cityList
        );
        return pageList;
    }

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
