package org.jeecg.modules.polymerize.drawflow.model;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @version 1.0
 * @description: Drawflow节点
 * @author: wayne
 * @date 2023/6/6 11:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DrawflowNode {

    /**起始节点*/
    public static String START_NODE = "StartNode";

    /**列表采集节点*/
    public static String LIST_RULE_NODE = "ListRuleNode";

    /**稿件详情采集节点*/
    public static String ARTICLE_RULE_NODE = "ArticleRuleNode";

    /**节点ID*/
    public String id;

    /**子节点列表*/
    public List<String> child;

    /**是否有子节点*/
    public boolean hasChild;

    /**父节点列表*/
    public List<String> parent;

    /**是否有父节点*/
    public boolean hasParent;

    /**节点数据*/
    public JSONObject data;

    /**节点类型: StartNode, ListRuleNode, ArticleRuleNode*/
    public String nodeType;

}
