package org.jeecg.modules.polymerize.service;

import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 爬虫临时数据存储
 * @Author: jeecg-boot
 * @Date:   2023-06-30
 * @Version: V1.0
 */
public interface ITmpCrawlDataService extends IService<TmpCrawlData> {

    boolean addTmpCrawlData(TmpCrawlData tmpCrawlData);

}
