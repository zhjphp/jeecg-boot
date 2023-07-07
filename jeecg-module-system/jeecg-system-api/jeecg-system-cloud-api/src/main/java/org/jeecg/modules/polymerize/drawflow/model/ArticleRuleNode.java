package org.jeecg.modules.polymerize.drawflow.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @version 1.0
 * @description: 稿件采集规则节点
 * @author: wayne
 * @date 2023/6/12 15:30
 */
@Data
public class ArticleRuleNode {

    public ArticleRuleNode(JSONObject obj) {
        checkRuleUrl = obj.getString("checkRuleUrl");
        customTags = obj.getString("customTags");
        singleFlag = obj.getBoolean("singleFlag");
        singleUrl = obj.getString("singleUrl");
        moreButtonMatch = obj.getString("moreButtonMatch");
        topicMatch = obj.getString("topicMatch");
        titleMatch = obj.getString("titleMatch");
        subtitleMatch = obj.getString("subtitleMatch");
        keywordsMatch = obj.getString("keywordsMatch");
        descriptionMatch = obj.getString("descriptionMatch");
        contentMatch = obj.getString("contentMatch");
        dateMatch = obj.getString("dateMatch");
        referenceMatch = obj.getString("referenceMatch");
        sourceMatch = obj.getString("sourceMatch");
        authorMatch = obj.getString("authorMatch");
        visitMatch = obj.getString("visitMatch");
        commentMatch = obj.getString("commentMatch");
        collectMatch = obj.getString("collectMatch");
        customConfig = obj.getString("customConfig");

    }

    /**测试规则url*/
    public String checkRuleUrl;

    /**自定义标签*/
    public String customTags;

    /**是否单页采集*/
    public Boolean singleFlag;

    /**单页采集url*/
    public String singleUrl;

    /**查看更多按钮*/
    public String moreButtonMatch;

    /**栏目匹配*/
    public String topicMatch;

    /**稿件标题匹配*/
    public String titleMatch;

    /**稿件副标题匹配*/
    public String subtitleMatch;

    /**稿件关键词匹配*/
    public String keywordsMatch;

    /**稿件描述匹配*/
    public String descriptionMatch;

    /**稿件正文匹配*/
    public String contentMatch;

    /**稿件日期匹配*/
    public String dateMatch;

    /**稿件出处匹配*/
    public String referenceMatch;

    /**稿件来源匹配*/
    public String sourceMatch;

    /**稿件作者匹配*/
    public String authorMatch;

    /**稿件访问量匹配*/
    public String visitMatch;

    /**稿件评论量匹配*/
    public String commentMatch;

    /**稿件收藏量匹配*/
    public String collectMatch;

    /**自定义配置*/
    public String customConfig;

}
