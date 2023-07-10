package org.jeecg.modules.polymerize.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.NoArgsConstructor;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.drawflow.model.ArticleResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 爬虫临时数据存储
 * @Author: jeecg-boot
 * @Date:   2023-06-30
 * @Version: V1.0
 */
@Data
@TableName("polymerize_tmp_crawl_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="tmp_crawl_data对象", description="爬虫临时数据存储")
@NoArgsConstructor
@AllArgsConstructor
public class TmpCrawlData implements Serializable {
    private static final long serialVersionUID = 1L;

    public TmpCrawlData(ArticleResult articleResult) {
        url = articleResult.getUrl();
        title = articleResult.getTitle();
        subtitle = articleResult.getSubtitle();
        keywords = articleResult.getKeywords();
        description = articleResult.getDescription();
        content = articleResult.getContent();
        date = articleResult.getDate();
        reference = articleResult.getReference();
        source = articleResult.getSource();
        author = articleResult.getAuthor();
        visit = articleResult.getVisit();
        comment = articleResult.getComment();
        collect = articleResult.getCollect();
        informationsourceid = articleResult.getInformationSourceId();
        taskid = articleResult.getTaskId();
        jobid = articleResult.getJobId();
        informationsourceName = articleResult.getInformationSourceName();
        informationsourceDomain = articleResult.getInformationSourceDomain();
        customTags = articleResult.getCustomTags();
        topic = articleResult.getTopic();
    }

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
	/**url*/
	@Excel(name = "url", width = 15)
    @ApiModelProperty(value = "url")
    private String url;
	/**标题*/
	@Excel(name = "标题", width = 15)
    @ApiModelProperty(value = "标题")
    private String title;
	/**副标题*/
	@Excel(name = "副标题", width = 15)
    @ApiModelProperty(value = "副标题")
    private String subtitle;
	/**关键字*/
	@Excel(name = "关键字", width = 15)
    @ApiModelProperty(value = "关键字")
    private String keywords;
	/**描述*/
	@Excel(name = "描述", width = 15)
    @ApiModelProperty(value = "描述")
    private String description;
	/**正文*/
	@Excel(name = "正文", width = 15)
    @ApiModelProperty(value = "正文")
    private String content;
	/**日期*/
	@Excel(name = "日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "日期")
    private String date;
	/**出处*/
	@Excel(name = "出处", width = 15)
    @ApiModelProperty(value = "出处")
    private String reference;
	/**来源*/
	@Excel(name = "来源", width = 15)
    @ApiModelProperty(value = "来源")
    private String source;
	/**作者*/
	@Excel(name = "作者", width = 15)
    @ApiModelProperty(value = "作者")
    private String author;
	/**访问量*/
	@Excel(name = "访问量", width = 15)
    @ApiModelProperty(value = "访问量")
    private String visit;
	/**评论量*/
	@Excel(name = "评论量", width = 15)
    @ApiModelProperty(value = "评论量")
    private String comment;
	/**收藏量*/
	@Excel(name = "收藏量", width = 15)
    @ApiModelProperty(value = "收藏量")
    private String collect;
	/**信源ID*/
	@Excel(name = "信源ID", width = 15)
    @ApiModelProperty(value = "信源ID")
    private String informationsourceid;
	/**信源任务ID*/
	@Excel(name = "信源任务ID", width = 15)
    @ApiModelProperty(value = "信源任务ID")
    private String taskid;
	/**任务调度ID*/
	@Excel(name = "任务调度ID", width = 15)
    @ApiModelProperty(value = "任务调度ID")
    private String jobid;
    /**错误数据*/
    @Excel(name = "错误数据", width = 15)
    @ApiModelProperty(value = "错误数据")
    private java.lang.Integer errorCode;
    /**任务调度ID*/
    @Excel(name = "错误原因", width = 15)
    @ApiModelProperty(value = "错误原因")
    private String reason;
    /**信源名称*/
    @Excel(name = "信源名称", width = 15)
    @ApiModelProperty(value = "信源名称")
    private String informationsourceName;
    /**信源域名*/
    @Excel(name = "信源域名", width = 15)
    @ApiModelProperty(value = "信源域名")
    private String informationsourceDomain;
    /**自定义标签*/
    @Excel(name = "自定义标签", width = 15)
    @ApiModelProperty(value = "自定义标签")
    private String customTags;
    /**栏目名称*/
    @Excel(name = "栏目名称", width = 15)
    @ApiModelProperty(value = "栏目名称")
    private String topic;
}
