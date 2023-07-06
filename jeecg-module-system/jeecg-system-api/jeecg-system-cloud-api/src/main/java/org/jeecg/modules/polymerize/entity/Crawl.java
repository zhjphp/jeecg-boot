package org.jeecg.modules.polymerize.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 爬虫表
 * @Author: jeecg-boot
 * @Date:   2023-05-10
 * @Version: V1.0
 */
@Data
@TableName("polymerize_crawl")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="crawl对象", description="爬虫表")
public class Crawl implements Serializable {
    private static final long serialVersionUID = 1L;

    /**java-playwright内置爬虫*/
    public static final Integer JAVA_PLAYWRIGHT_INTERNAL = 1;

    /**python-独立爬虫*/
    public static final Integer PYTHON_ALONE = 2;

    /**php-独立爬虫*/
    public static final Integer PHP_ALONE = 3;

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
    /**爬虫类型(1:java-playwright内置爬虫,2:python-独立爬虫,3:php-独立爬虫)*/
    @Dict(dicCode = "crawl_type")
    @Excel(name = "爬虫类型(1:java-playwright内置爬虫,2:python-独立爬虫,3:php-独立爬虫)", width = 15)
    @ApiModelProperty(value = "爬虫类型(1:java-playwright内置爬虫,2:python-独立爬虫,3:php-独立爬虫)")
    private Integer type;
	/**仓库地址*/
	@Excel(name = "仓库地址", width = 15)
    @ApiModelProperty(value = "仓库地址")
    private String repository;
    /**预处理指令*/
    @Excel(name = "预处理指令", width = 15)
    @ApiModelProperty(value = "预处理指令")
    private String preCommand;
	/**执行指令*/
	@Excel(name = "执行指令", width = 15)
    @ApiModelProperty(value = "执行指令")
    private String runCommand;
	/**代码分支*/
	@Excel(name = "代码分支", width = 15)
    @ApiModelProperty(value = "代码分支")
    private String branch;
	/**代码版本号*/
	@Excel(name = "代码版本号", width = 15)
    @ApiModelProperty(value = "代码版本号")
    private String version;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private String remark;
	/**删除状态*/
	@Excel(name = "删除状态", width = 15)
    @ApiModelProperty(value = "删除状态")
    @TableLogic
    private Integer delFlag;
}
