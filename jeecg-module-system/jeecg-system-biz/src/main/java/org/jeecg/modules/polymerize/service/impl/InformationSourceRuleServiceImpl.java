package org.jeecg.modules.polymerize.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.drawflow.model.ArticleRuleNode;
import org.jeecg.modules.polymerize.drawflow.model.ListRuleNode;
import org.jeecg.modules.polymerize.entity.InformationSourceRule;
import org.jeecg.modules.polymerize.mapper.InformationSourceRuleMapper;
import org.jeecg.modules.polymerize.playwright.CheckRulePlaywrightCrawl;
import org.jeecg.modules.polymerize.service.IInformationSourceRuleService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Description: 信源规则
 * @Author: jeecg-boot
 * @Date:   2023-04-29
 * @Version: V1.0
 */
@Service
public class InformationSourceRuleServiceImpl extends ServiceImpl<InformationSourceRuleMapper, InformationSourceRule> implements IInformationSourceRuleService {

    @Resource
    InformationSourceRuleMapper informationSourceRuleMapper;

    @Override
    @Transactional
    public void configureRule(String informationSourceId, String drawflowConfig) throws JeecgBootException {
        if (oConvertUtils.isEmpty(informationSourceId)) {
            throw new JeecgBootException("信源ID为空");
        }
        // 先删
        informationSourceRuleMapper.delete(new LambdaQueryWrapper<InformationSourceRule>().eq(InformationSourceRule::getInformationSourceId, informationSourceId));
        // 后入
        InformationSourceRule informationSourceRule = new InformationSourceRule();
        informationSourceRule.setInformationSourceId(informationSourceId);
        informationSourceRule.setDrawflowConfig(drawflowConfig);
        if (informationSourceRuleMapper.insert(informationSourceRule) != 1) {
            log.error("配置规则失败");
            throw new JeecgBootException("配置规则失败");
        }
    }

    @Override
    public JSONObject checkListRule(ListRuleNode listRuleNode) throws Exception {
        CheckRulePlaywrightCrawl checkRulePlaywrightCrawl = SpringContextUtils.getApplicationContext().getBean(CheckRulePlaywrightCrawl.class);
        JSONObject result = checkRulePlaywrightCrawl.testGetList(listRuleNode);
        return result;
    }

    @Override
    public JSONObject checkArticleRule(ArticleRuleNode articleRuleNode) throws Exception {
        CheckRulePlaywrightCrawl checkRulePlaywrightCrawl = SpringContextUtils.getApplicationContext().getBean(CheckRulePlaywrightCrawl.class);
        JSONObject result = checkRulePlaywrightCrawl.testGetArticle(articleRuleNode);
        return result;
    }

}
