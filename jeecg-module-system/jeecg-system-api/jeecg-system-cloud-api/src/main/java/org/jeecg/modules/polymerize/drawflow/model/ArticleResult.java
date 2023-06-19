package org.jeecg.modules.polymerize.drawflow.model;

import lombok.Data;

/**
 * @version 1.0
 * @description: 稿件详情采集结果
 * @author: wayne
 * @date 2023/6/12 15:52
 */
@Data
public class ArticleResult {

    /**稿件url*/
    public String url;

    /**稿件标题*/
    public String title;

    /**稿件副标题*/
    public String subtitle;

    /**稿件关键词*/
    public String keywords;

    /**稿件描述*/
    public String description;

    /**稿件正文*/
    public String content;

    /**稿件日期*/
    public String date;

    /**稿件出处*/
    public String reference;

    /**稿件来源*/
    public String source;

    /**稿件作者*/
    public String author;

    /**稿件访问量*/
    public String visit;

    /**稿件评论量*/
    public String comment;

    /**稿件收藏量*/
    public String collect;

}
