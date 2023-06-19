package org.jeecg.modules.polymerize.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.polymerize.drawflow.model.ArticleRuleNode;
import org.jeecg.modules.polymerize.drawflow.model.ListRuleNode;
import org.jeecg.modules.polymerize.entity.InformationSourceRule;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 信源规则
 * @Author: jeecg-boot
 * @Date:   2023-04-29
 * @Version: V1.0
 */
public interface IInformationSourceRuleService extends IService<InformationSourceRule> {

    void configureRule(String informationSourceId, String drawflowConfig) throws JeecgBootException;

    JSONObject checkListRule(ListRuleNode listRuleNode) throws Exception;

    JSONObject checkArticleRule(ArticleRuleNode articleRuleNode) throws Exception;

}
