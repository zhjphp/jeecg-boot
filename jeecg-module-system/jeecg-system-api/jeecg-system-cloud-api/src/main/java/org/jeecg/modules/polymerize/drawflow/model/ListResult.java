package org.jeecg.modules.polymerize.drawflow.model;

import lombok.Data;

import java.util.Date;

/**
 * @version 1.0
 * @description: 列表页采集结果
 * @author: wayne
 * @date 2023/6/12 15:52
 */
@Data
public class ListResult {

    /**稿件标题*/
    String title;

    /**稿件日期*/
    String date;

    /**稿件url*/
    String url;

}
