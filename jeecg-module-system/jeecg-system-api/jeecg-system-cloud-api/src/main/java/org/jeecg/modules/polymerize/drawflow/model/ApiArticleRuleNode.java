package org.jeecg.modules.polymerize.drawflow.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/9/20 16:37
 */
@Data
public class ApiArticleRuleNode {


    public ApiArticleRuleNode(JSONObject obj) {
        checkRuleUrl = obj.getString("checkRuleUrl");
        checkRuleDisableLoadResource = obj.getString("checkRuleDisableLoadResource");
        customTags = obj.getString("customTags");
        singleFlag = obj.getBoolean("singleFlag");
        articleReqUrl = obj.getString("articleReqUrl");
        articleCustomParam = obj.getString("articleCustomParam");
        method = obj.getString("method");
        contentType = obj.getString("contentType");
        reqHeader = obj.getString("reqHeader");
        reqBody = obj.getString("reqBody");
        reqUrlParam = obj.getString("reqUrlParam");
        resultPreprocessor = obj.getString("resultPreprocessor");
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
    }

    /**测试规则url*/
    public String checkRuleUrl;

    /**资源屏蔽配置*/
    public String checkRuleDisableLoadResource;

    /**自定义标签*/
    public String customTags;

    /**是否单页采集*/
    public Boolean singleFlag;

    /**目标url*/
    public String articleReqUrl;

    /**
     * 自定义参数
     */
    public String articleCustomParam;

    /**请求method*/
    public String method;

    /**请求contentType*/
    public String contentType;

    /**请求header*/
    public String reqHeader;

    /**请求body*/
    public String reqBody;

    /**请求body*/
    public String reqUrlParam;

    /**请求结果预处理指令*/
    public String resultPreprocessor;

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


}
