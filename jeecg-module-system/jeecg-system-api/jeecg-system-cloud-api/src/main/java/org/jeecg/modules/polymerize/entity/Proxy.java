package org.jeecg.modules.polymerize.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: ip代理
 * @Author: jeecg-boot
 * @Date:   2023-06-01
 * @Version: V1.0
 */
@Data
@TableName("polymerize_proxy")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="proxy对象", description="ip代理")
public class Proxy implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
	/**名称*/
	@Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
    private String name;
	/**代理类型*/
    @Dict(dicCode = "proxy_type")
	@Excel(name = "代理类型", width = 15)
    @ApiModelProperty(value = "代理类型")
    private Integer type;
	/**接口地址*/
	@Excel(name = "接口地址", width = 15)
    @ApiModelProperty(value = "接口地址")
    private String apiUrl;
	/**类全名*/
	@Excel(name = "类全名", width = 15)
    @ApiModelProperty(value = "类全名")
    private String fullClassName;
	/**启用状态*/
    @Dict(dicCode = "proxy_use_flag")
	@Excel(name = "启用状态", width = 15)
    @ApiModelProperty(value = "启用状态")
    private Integer status;
	/**排序*/
	@Excel(name = "排序", width = 15)
    @ApiModelProperty(value = "排序")
    private Integer rank;
	/**删除状态*/
	@Excel(name = "删除状态", width = 15)
    @ApiModelProperty(value = "删除状态")
    @TableLogic
    private Integer delFlag;
}
