package org.jeecg.modules.polymerize.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @version 1.0
 * @description: TmpCrawlDataDTO
 * @author: wayne
 * @date 2023/7/6 16:34
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class TmpCrawlDataDTO {

    private String informationSourceName;

    private String informationSourceDomain;

    private String topic;

    private String customTags;

    private String informationsourceid;

    private String taskid;

    private Integer errorCode;

    private String startCreateTime;

    private String endCreateTime;

}
