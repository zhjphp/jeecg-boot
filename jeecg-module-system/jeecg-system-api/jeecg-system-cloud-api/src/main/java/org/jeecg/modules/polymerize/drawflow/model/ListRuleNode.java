package org.jeecg.modules.polymerize.drawflow.model;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @version 1.0
 * @description: 列表采集规则节点
 * @author: wayne
 * @date 2023/6/7 10:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListRuleNode {

    public ListRuleNode(JSONObject obj) {
        checkRuleUrl = obj.getString("checkRuleUrl");
        effectiveDays = obj.getInteger("effectiveDays");
        startTime = obj.getDate("startTime");
        endTime = obj.getDate("endTime");
        moreMatch = obj.getString("moreMatch");
        waterfallFlag = obj.getBoolean("waterfallFlag");
        waterfallPageCount = obj.getInteger("waterfallPageCount");
        waterfallBottomMatch = obj.getString("waterfallBottomMatch");
        startUrls = obj.getString("startUrls");
        pageMatch = obj.getString("pageMatch");
        pageDepth = obj.getInteger("pageDepth");
        totalPageMatch = obj.getString("totalPageMatch");
        nextMatch = obj.getString("nextMatch");
        enableOutside = obj.getBoolean("enableOutside");
        articleUrlMatch = obj.getString("articleUrlMatch");
        articleTitleMatch = obj.getString("articleTitleMatch");
        articleDateMatch = obj.getString("articleDateMatch");
        customConfig = obj.getString("customConfig");
    }

    /**测试规则url*/
    public String checkRuleUrl;

    /**有效天数*/
    public Integer effectiveDays;

    /**起始时间*/
    public Date startTime;

    /**终止时间*/
    public Date endTime;

    /**查看更多按钮*/
    public String moreMatch;

    /**是否为瀑布流*/
    public Boolean waterfallFlag;

    /**瀑布流下拉屏数*/
    public Integer waterfallPageCount;

    /**瀑布流底部标识匹配*/
    public String waterfallBottomMatch;

    /**起始url列表,多个用","隔开*/
    public String startUrls;

    /**列表区块匹配*/
    public String pageMatch;

    /**总页数匹配*/
    public String totalPageMatch;

//    /**上一页匹配*/
//    public String preMatch;

    /**翻页深度*/
    public Integer pageDepth;

    /**下一页匹配*/
    public String nextMatch;

    /**是否爬取外链*/
    public Boolean enableOutside;

    /**稿件url匹配*/
    public String articleUrlMatch;

    /**稿件标题匹配*/
    public String articleTitleMatch;

    /**稿件日期配*/
    public String articleDateMatch;

    /**自定义配置*/
    public String customConfig;

}
