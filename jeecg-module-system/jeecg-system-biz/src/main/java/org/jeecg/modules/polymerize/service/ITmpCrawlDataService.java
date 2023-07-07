package org.jeecg.modules.polymerize.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.polymerize.dto.TmpCrawlDataDTO;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 爬虫临时数据存储
 * @Author: jeecg-boot
 * @Date:   2023-06-30
 * @Version: V1.0
 */
public interface ITmpCrawlDataService extends IService<TmpCrawlData> {

    IPage<TmpCrawlData> queryTmpCrawlData(TmpCrawlDataDTO tmpCrawlDataDTO, Integer pageNo, Integer pageSize);

    boolean addTmpCrawlData(TmpCrawlData tmpCrawlData);

}
