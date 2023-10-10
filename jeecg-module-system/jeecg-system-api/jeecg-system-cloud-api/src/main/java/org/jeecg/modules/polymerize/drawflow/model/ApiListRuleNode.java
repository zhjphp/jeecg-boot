package org.jeecg.modules.polymerize.drawflow.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Date;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/8/31 11:33
 */
@Data
public class ApiListRuleNode {

    public ApiListRuleNode() {}

    public ApiListRuleNode(JSONObject obj) {
        checkRuleUrl = obj.getString("checkRuleUrl");
        startUrls = obj.getString("startUrls");
        inListFlag = obj.getBoolean("inListFlag");
        effectiveDays = obj.getInteger("effectiveDays");
        startTime = obj.getDate("startTime");
        endTime = obj.getDate("endTime");
        resultPreprocessor = obj.getString("resultPreprocessor");
        listMatch = obj.getString("listMatch");
        articleIdMatch = obj.getString("articleIdMatch");
        articleTitleMatch = obj.getString("articleTitleMatch");
        articleDateMatch = obj.getString("articleDateMatch");
        pageCountMatch = obj.getString("pageCountMatch");
        totalPageMatch = obj.getString("totalPageMatch");
        pageSizeMatch = obj.getString("pageSizeMatch");
        totalCountMatch = obj.getString("totalCountMatch");
        customParam = obj.getString("customParam");
        method = obj.getString("method");
        contentType = obj.getString("contentType");
        reqHeader = obj.getString("reqHeader");
        reqBody = obj.getString("reqBody");
        reqUrlParam = obj.getString("reqUrlParam");
        pageDepth = obj.getInteger("pageDepth");
        resultCustomParam = obj.getJSONArray("resultCustomParam");
    }

    /**测试页URL*/
    public String checkRuleUrl;

    /**起始URL*/
    public String startUrls;

    /**在列表页中采集,不进入详情页*/
    public Boolean inListFlag;

    /**有效天数*/
    public Integer effectiveDays;

    /**翻页深度*/
    public Integer pageDepth;

    /**起始时间*/
    public Date startTime;

    /**终止时间*/
    public Date endTime;

    /**请求结果预处理指令*/
    public String resultPreprocessor;

    /**列表区块匹配*/
    public String listMatch;

    /**详情id匹配*/
    public String articleIdMatch;

    /**标题匹配*/
    public String articleTitleMatch;

    /**日期匹配*/
    public String articleDateMatch;

    /**当前页码匹配*/
    public String pageCountMatch;

    /**总页数匹配*/
    public String totalPageMatch;

    /**页容量匹配*/
    public String pageSizeMatch;

    /**总稿件数量匹配*/
    public String totalCountMatch;

    /**自定义配置*/
    public String customParam;

    /**请求方法*/
    public String method;

    /**请求contentType*/
    public String contentType;

    /**请求header*/
    public String reqHeader;

    /**请求body*/
    public String reqBody;

    /**请求body*/
    public String reqUrlParam;

    /**自定义结果抽取参数*/
    public JSONArray resultCustomParam;

    /**栏目匹配*/
    public String topicMatch;

    /**稿件副标题匹配*/
    public String subtitleMatch;

    /**稿件关键词匹配*/
    public String keywordsMatch;

    /**稿件描述匹配*/
    public String descriptionMatch;

    /**稿件正文匹配*/
    public String contentMatch;

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
