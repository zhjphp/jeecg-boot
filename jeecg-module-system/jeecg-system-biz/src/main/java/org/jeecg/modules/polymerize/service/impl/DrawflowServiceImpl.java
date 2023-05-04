package org.jeecg.modules.polymerize.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.service.IDrawflowService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/4/28 13:43
 */
@Slf4j
@Service
public class DrawflowServiceImpl implements IDrawflowService {

    public static String DRAWFLOW_JSON_OUTER_KEY = "drawflow";
    public static String DRAWFLOW_MODULE_DATA_KEY = "data";
    public static String DRAWFLOW_MODULE_START_NODE_NAME = "startNode";
    public static String DRAWFLOW_MODULE_OUTPUTS_KEY = "outputs";
    public static String DRAWFLOW_MODULE_INPUTS_KEY = "inputs";
    public static String DRAWFLOW_MODULE_CONNECTIONS_NODEID_KEY = "node";
    public static String DRAWFLOW_MODULE_CONNECTIONS_OUTPUT_KEY = "output";

    // parseModule
    @Override
    public void test(JSONObject jSONObject) {
        log.info("jSONObject 请求：" + jSONObject.toString());
        int drawflowModuleCount = 0;
        JSONObject drawflow = jSONObject.getJSONObject(DRAWFLOW_JSON_OUTER_KEY);

        // 遍历 drawflow 数据中的 module
        log.info("开始遍历 drawflow 数据中的 module");
        for (Map.Entry entry : drawflow.entrySet()) {
            drawflowModuleCount++;
            String key = (String) entry.getKey();
            log.info("第[" + drawflowModuleCount + "]个 key：" + key);
            // 取出 module 中的数据
            JSONObject moduleData = drawflow.getJSONObject(key).getJSONObject(DRAWFLOW_MODULE_DATA_KEY);
            log.info("json 数据为：" + moduleData.toString());

            int moduleNodeCount = 0;
            // 遍历 moduleData,找出 start 节点
            for (Map.Entry moduleEntry : moduleData.entrySet()) {
                moduleNodeCount++;
                String moduleKey = (String) moduleEntry.getKey();
                JSONObject node = moduleData.getJSONObject(moduleKey);
                // 获取节点名称
                String nodeName = node.getString("name");
                // 判断是否为start
                if (nodeName.equals(DRAWFLOW_MODULE_START_NODE_NAME)) {
                    // 如果为start节点,则继续查找next节点
                    JSONObject outputs = node.getJSONObject(DRAWFLOW_MODULE_OUTPUTS_KEY);
                    int outputsCount = 0;
                    // 遍历outputs端点
                    for (Map.Entry outputsEntry : outputs.entrySet()) {
                        outputsCount++;
                        String outputsKey = (String) outputsEntry.getKey();
                        JSONArray connections = outputs.getJSONArray(outputsKey);
                        int connectionsCount = connections.size();
                        // 遍历connections所有链接的节点
                        for (int i = 0; i< connectionsCount; i++) {
                            JSONObject connection = connections.getJSONObject(i);
                            String nextNodeId = connection.getString(DRAWFLOW_MODULE_CONNECTIONS_NODEID_KEY);
                            String nextNodeOutPut = connection.getString(DRAWFLOW_MODULE_CONNECTIONS_OUTPUT_KEY);
                        }
                    }
                }
            }
        }
    }



}
