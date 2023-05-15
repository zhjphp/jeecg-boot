package org.jeecg.modules.polymerize.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 信源规则
 * @Author: jeecg-boot
 * @Date:   2023-04-29
 * @Version: V1.0
 */
@Data
@TableName("polymerize_information_source_rule")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="information_source_rule对象", description="信源规则")
public class InformationSourceRule implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DRAWFLOW_DEFAULT_CONFIG = "{\"drawflow\":{\"Home\":{\"data\":{}}}}";

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**信源ID*/
	@Excel(name = "信源ID", width = 15)
    @ApiModelProperty(value = "信源ID")
    private String informationSourceId;
	/**drawflow 配置*/
	@Excel(name = "drawflow 配置", width = 15)
    @ApiModelProperty(value = "drawflow 配置")
    private String drawflowConfig;
}
