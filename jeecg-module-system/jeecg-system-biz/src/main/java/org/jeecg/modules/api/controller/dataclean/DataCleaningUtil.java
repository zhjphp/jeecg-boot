package org.jeecg.modules.api.controller.dataclean;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;

/**
 * @version 1.0
 * @description: DataCleaningUtil For Dzw test
 * @author: wyx
 * @date 2023/10/20 15:17
 */
public class DataCleaningUtil {

    public static void tmpCrawlDataCleaningForDzw(TmpCrawlData tmpCrawlData){

        if(tmpCrawlData != null && StringUtils.isNotEmpty(tmpCrawlData.getContent())){
            String content = tmpCrawlData.getContent();
            content = content.replace("<img> play stop mute max volume 00:00 00:00 repeat &nbsp;","");
            tmpCrawlData.setContent(content);
        }

    }
}
