package org.jeecg.modules.polymerize.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.polymerize.entity.InformationSource;

import java.io.Serializable;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/4/17 22:17
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value="information_source_dto对象", description="信源管理")
public class InformationSourceDTO extends InformationSource implements Serializable {
    private static final long serialVersionUID = 1L;

    /**分类IDS*/
    @ApiModelProperty(value = "分类IDS")
    private String categoryIds;

    /**分类ID*/
    @ApiModelProperty(value = "分类ID")
    private String categoryId;

}
