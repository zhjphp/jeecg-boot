package org.jeecg.modules.polymerize.drawflow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.drawflow.model.DrawflowNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @description: 信源规则
 * @author: wayne
 * @date 2023/6/6 10:56
 */
@Slf4j
public class Drawflow {

    /**Drawflow导出的json配置*/
    private String jsonConfig;

    /**默认tab页数据*/
    private Map<String, Object> defaultTab;

    /**所有节点的map结构,key为节点ID*/
    private Map<String, DrawflowNode> nodeMap = new HashMap<>();

    /**迭代器的定位器*/
    private int position = 0;

    private List<DrawflowNode> startNodeList;

    public Drawflow(String jsonConfig) throws Exception {
        this.jsonConfig = jsonConfig;
        this.defaultTab = JSON.parseObject(jsonConfig).getJSONObject("drawflow").getJSONObject("Home").getJSONObject("data").getInnerMap();
        log.info("defaultTab: {}", this.defaultTab.toString());
        convertData();
        prepareIterator();
    }

    /**
     * 初始化数据
     */
    private void convertData() {
        defaultTab.forEach((key, value) -> {
            DrawflowNode node = new DrawflowNode();
            JSONObject data = (JSONObject) JSON.toJSON(value);
            log.info("defaultTab-data: {}", JSONObject.toJSONString(data));
            // 转换node数据
            node.setId(key);
            log.info("setId: {}", key);
            node.setNodeType(data.getString("name"));
            log.info("setNodeType: {}", data.getString("name"));
            node.setData(data.getJSONObject("data"));
            log.info("setData: {}", data.getJSONObject("data").toJSONString());
            // 获取node的outputs节点
            JSONObject outputs = data.getJSONObject("outputs");
            log.info("outputs: {}", outputs.toString());
            if (outputs.size() == 0) {
                node.setChild(null);
                node.setHasChild(false);
                log.info("setNext: null");
            } else {
                JSONObject output_1 = outputs.getJSONObject("output_1");
                log.info("output_1: {}", output_1.toString());
                JSONArray outputConnections = output_1.getJSONArray("connections");
                if (outputConnections.size() == 0) {
                    node.setChild(null);
                    node.setHasChild(false);
                    log.info("setNext: null");
                } else {
                    List<String> nextList = new ArrayList<>();
                    for (int i =0; i < outputConnections.size(); i++) {
                        JSONObject connection = outputConnections.getJSONObject(i);
                        nextList.add(connection.getString("node"));
                    }
                    node.setChild(nextList);
                    node.setHasChild(true);
                    log.info("setNext: {}", nextList.toString());
                }
            }

            // 获取node的inputs节点
            // start 节点没有上级
            if (node.getNodeType().equals(DrawflowNode.START_NODE)) {
                node.setParent(null);
                node.setHasParent(false);
                log.info("setNext: null, start node no prev");
            } else {
                // 获取上级的所有节点
                JSONObject inputs = data.getJSONObject("inputs");
                JSONObject input_1 = inputs.getJSONObject("input_1");
                JSONArray inputConnections = input_1.getJSONArray("connections");
                // 遍历取出上级节点,并转换为list,为 pre, hasPrev 赋值
                if (inputConnections.size() == 0) {
                    node.setParent(null);
                    node.setHasParent(false);
                    log.info("setNext: null");
                } else {
                    List<String> prevList = new ArrayList<>();
                    for (int i =0; i < inputConnections.size(); i++) {
                        JSONObject connection = inputConnections.getJSONObject(i);
                        prevList.add(connection.getString("node"));
                    }
                    node.setParent(prevList);
                    node.setHasParent(true);
                    log.info("setPrev: {}", prevList.toString());
                }
            }
            log.info("put node: {}", node.toString());
            nodeMap.put(node.getId(), node);
        });
    }

    /**
     * 根据节点ID获取节点
     *
     * @param nodeId
     * @return
     */
    public DrawflowNode getNode(String nodeId) {
        return nodeMap.get(nodeId);
    }

    /**
     * 是否还有下一个StartNode节点
     *
     * @return
     */
    public boolean hasNext() {
        if (position < startNodeList.size()) {
            return true;
        } else {
            position = 0;
            return false;
        }
    }

    /**
     * 遍历StartNode节点,只迭代第一层
     *
     * @return
     */
    public DrawflowNode next() {
        DrawflowNode node = startNodeList.get(position);
        position++;
        return node;
    }

    /**
     * 准备迭代数据
     *
     * @return
     */
    private void prepareIterator() {
        startNodeList = nodeMap.values().stream().filter((value) -> {
            return value.getNodeType().equals(DrawflowNode.START_NODE);
        }).collect(Collectors.toList());
    }

    /**
     * 取出StartNode节点
     *
     * @return
     */
    public List<DrawflowNode> getStartNodeList() {
        return startNodeList;
    }

}
