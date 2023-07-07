package org.jeecg.modules.polymerize.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.polymerize.dto.TmpCrawlDataDTO;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.polymerize.vo.InformationSourceVO;

/**
 * @Description: 爬虫临时数据存储
 * @Author: jeecg-boot
 * @Date:   2023-06-30
 * @Version: V1.0
 */
public interface TmpCrawlDataMapper extends BaseMapper<TmpCrawlData> {

    IPage<TmpCrawlData> queryTmpCrawlData(
            Page<TmpCrawlData> page,
            @Param("informationSourceName") String informationSourceName,
            @Param("informationSourceDomain") String informationSourceDomain,
            @Param("topic") String topic,
            @Param("customTagList") List<String> customTagList,
            @Param("informationsourceid") String informationsourceid,
            @Param("taskid") String taskid,
            @Param("errorCode") Integer errorCode
    );

}
