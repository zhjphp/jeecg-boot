package org.jeecg.modules.polymerize.drawflow.model;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @description: StartNode
 * @author: wayne
 * @date 2023/8/1 17:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartRuleNode {

    public StartRuleNode(JSONObject obj) {
        disableLoadResource = obj.getString("disableLoadResource");
    }

    /**屏蔽加载资源配置*/
    public String disableLoadResource;

}
