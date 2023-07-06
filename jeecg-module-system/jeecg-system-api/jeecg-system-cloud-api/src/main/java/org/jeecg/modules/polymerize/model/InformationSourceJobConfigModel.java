package org.jeecg.modules.polymerize.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/5/12 23:17
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class InformationSourceJobConfigModel {

    public Long jobId;

    public String taskId;

    public String taskName;

    public String CrawlId;

    public String CrawlName;

    public Integer CrawlType;

    public String repository;

    public String runCommand;

    public String preCommand;

    public Long timeout;

    public String branch;

    public String version;

    public String informationSourceId;

    public String informationSourceName;

    public String domain;

    public String scheme;

    public Integer port;

    public String rule;

    public String ipProxyApi;

}
