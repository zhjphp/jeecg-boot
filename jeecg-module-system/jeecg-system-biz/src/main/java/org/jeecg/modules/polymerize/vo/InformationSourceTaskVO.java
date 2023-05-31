package org.jeecg.modules.polymerize.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.polymerize.entity.InformationSourceTask;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/5/10 14:23
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="InformationSourceTaskVO对象", description="信源任务管理")
public class InformationSourceTaskVO extends InformationSourceTask {
    private static final long serialVersionUID = 1L;

    /**爬虫名称*/
    @ApiModelProperty(value = "爬虫名称")
    private String crawlName;

    public InformationSourceTaskVO(InformationSourceTask informationSourceTask) {
        this.setId(informationSourceTask.getId());
        this.setCreateBy(informationSourceTask.getCreateBy());
        this.setCreateTime(informationSourceTask.getCreateTime());
        this.setUpdateBy(informationSourceTask.getUpdateBy());
        this.setUpdateTime(informationSourceTask.getUpdateTime());
        this.setSysOrgCode(informationSourceTask.getSysOrgCode());
        this.setName(informationSourceTask.getName());
        this.setCrawlId(informationSourceTask.getCrawlId());
        this.setTimeout(informationSourceTask.getTimeout());
        this.setContent(informationSourceTask.getContent());
        this.setDelFlag(informationSourceTask.getDelFlag());
    }
}
