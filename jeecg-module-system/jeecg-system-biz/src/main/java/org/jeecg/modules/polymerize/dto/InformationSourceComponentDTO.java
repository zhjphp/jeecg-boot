package org.jeecg.modules.polymerize.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/5/9 14:45
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="前端 JSelectInformationSourceByCategory 组件使用", description="任务中添加信源")
public class InformationSourceComponentDTO extends InformationSourceDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分页")
    private int pageNo = 1;

    @ApiModelProperty(value = "页容量")
    private int pageSize = 10;

}
