package org.jeecg.modules.polymerize.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: 分类字典
 * @Author: wayne
 * @Date:   2023-04-16
 * @Version: V1.0
 */
@Data
@TableName("polymerize_category")
public class PolymerizeCategory implements Serializable,Comparable<PolymerizeCategory> {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private java.lang.String id;
    /**父级节点*/
    private java.lang.String pid;
    /**类型名称*/
    @Excel(name = "类型名称", width = 15)
    private java.lang.String name;
    /**类型编码*/
    @Excel(name = "类型编码", width = 15)
    private java.lang.String code;
    /**创建人*/
    private java.lang.String createBy;
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
    /**更新人*/
    private java.lang.String updateBy;
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
    /**所属部门*/
    private java.lang.String sysOrgCode;
    /**是否有子节点*/
    @Excel(name = "是否有子节点(1:有)", width = 15)
    private java.lang.String hasChild;
    /**删除状态*/
    @Excel(name = "删除状态", width = 15)
    @ApiModelProperty(value = "删除状态")
    @TableLogic
    private Integer delFlag;

    /**租户ID*/
    private java.lang.Integer tenantId;

    @Override
    public int compareTo(PolymerizeCategory o) {
        //比较条件我们定的是按照code的长度升序
        // <0：当前对象比传入对象小。
        // =0：当前对象等于传入对象。
        // >0：当前对象比传入对象大。
        int	 s = this.code.length() - o.code.length();
        return s;
    }
    @Override
    public String toString() {
        return "PolymerizeCategory [code=" + code + ", name=" + name + "]";
    }

}
