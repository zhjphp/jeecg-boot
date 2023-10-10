package org.jeecg.modules.polymerize.drawflow.model;

import lombok.Data;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/9/22 9:19
 */
@Data
public class ApiListResult {

    /**接口地址*/
    String apiUrl;

    /**稿件标题*/
    String title;

    /**稿件日期*/
    String date;

    /**稿件唯一标识*/
    String articleId;

}
