package org.jeecg.modules.polymerize.playwright.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.plugins.inteface.PluginInterface;
import org.jeecg.modules.polymerize.plugins.util.PluginUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * 常规表达式:
 * json=some expression
 * value=some expression
 * set=some expression
 * regex=some expression
 * plugin=some expression
 * addi=some expression
 *
 * 变量定义,名字前加"$"符号:
 * $paramName=some string
 *
 * jsonPath表达式:
 * https://github.com/json-path/JsonPath
 *
 * 使用变量时,占位符表达式: ${paramName.valueType}
 * key=somestring${paramName.valueType}somestring
 *
 * json请求需要确定数据类型:
 * key->boolean=true
 * key->int=123
 * key->float=1.23
 * key=some string
 *
 *
 * @version 1.0
 * @description: Api爬虫规则解析
 * @author: wayne
 * @date 2023/8/31 15:44
 */
@Component
@Slf4j
@RefreshScope
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ApiParser {

    /**匹配表达式类型前缀*/
    public enum ExprType {

        JSON("json"),

        VALUE("value"),

        SET("set"),

        REGEX("regex"),

        PLUGIN("plugin"),

        FUNC("func"),

        ADDITIONAL("addi");

        public final String prefix;

        ExprType(String prefix) {
            this.prefix = prefix;
        }
    }

    /**自定义变量参数池*/
    public Map<String, String> paramPool = new HashMap<>();

    /**
     * 自定义参数命名规则: $abc_123
     * 自定义参数引用规则: ${abc_123}
     */
    public String paramNameRegex = "\\$\\{([a-zA-Z0-9_]*?)\\}";

    @Data
    public class Node {
        private ExprType type;
        private String expr;
    }

    private String jsonData;

    @Resource
    private PluginUtil pluginUtil;

    /**插件存放目录*/
    @Value("${polymerize.apiCrawl.pluginUrl}")
    private String pluginUrl;

    public String doParse(String json, String rule, String paramName, Boolean isAddParamPool) throws Exception {
        log.info("doParse: -------------");
        LinkedList<Node> nodeList = parser(rule);
        String result = actuator(nodeList, json, paramName, isAddParamPool);
        return result;
    }

    /**
     * 解析表达式组到列表中
     *
     * @param match 匹配表达式
     */
    public LinkedList<Node> parser(String match) throws RuntimeException {
        if (oConvertUtils.isEmpty(match)) {
            throw new RuntimeException("match 表达式为空");
        }
        // 1. 按换行符 \n 拆分表达式为 list
        String[] matchArray = match.split("\n");

        log.info("按顺序转换为 LinkedList 结构");
        // 2. 按顺序转换为 LinkedList 结构
        LinkedList<Node> nodeList = new LinkedList<>();
        for (int i = 0; i < matchArray.length; i++) {
            ApiParser.Node node = new ApiParser.Node();
            String tmpMatch = matchArray[i].trim();
            if (tmpMatch.startsWith(ExprType.JSON.prefix)) {
                String expr = tmpMatch.replace(ExprType.JSON.prefix + "=", "");
                node.setType(ExprType.JSON);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(ExprType.VALUE.prefix)) {
                String expr = tmpMatch.replace(ExprType.VALUE.prefix + "=", "");
                node.setType(ExprType.VALUE);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(ExprType.PLUGIN.prefix)) {
                String expr = tmpMatch.replace(ExprType.PLUGIN.prefix + "=", "");
                node.setType(ExprType.PLUGIN);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(ExprType.REGEX.prefix)) {
                String expr = tmpMatch.replace(ExprType.REGEX.prefix + "=", "");
                node.setType(ExprType.REGEX);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(ExprType.SET.prefix)) {
                String expr = tmpMatch.replace(ExprType.SET.prefix + "=", "");
                node.setType(ExprType.SET);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(ExprType.FUNC.prefix)) {
                String expr = tmpMatch.replace(ExprType.FUNC.prefix + "=", "");
                node.setType(ExprType.FUNC);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(ExprType.ADDITIONAL.prefix)) {
                String expr = tmpMatch.replace(ExprType.ADDITIONAL.prefix + "=", "");
                node.setType(ExprType.ADDITIONAL);
                node.setExpr(expr.trim());
            }  else {
                throw new RuntimeException("不支持的表达式类型: " + tmpMatch);
            }
            nodeList.add(node);
        }

        log.info("LinkedList 结构转换完成: {}", nodeList.toString());
        return nodeList;
    }

    /**
     * 按解析出的表达式组,分别执行表达式
     *
     * @param nodeList 表达式组
     * @param json 接口返回的json数据
     */
    private String actuator(LinkedList<Node> nodeList, String json, String paramName, Boolean isAddParamPool) throws Exception {
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
        String result = null;
        // 正则匹配使用
        int time = 0;
        while (nodeList.size() > 0) {
            // 先进先出,取出 node
            Node node = nodeList.pop();
            if (oConvertUtils.isEmpty(node.getExpr())) {
                // 如果表达式为空,则进入下一条表达式
                continue;
            }
            // 解析表达式中包含的变量
            node.setExpr(parseParamPlaceholder(node.getExpr()));
            // 执行node
            switch (node.getType()) {
                case JSON:
                    log.info("执行JSON表达式");
                    Configuration conf1 = conf.addOptions(Option.ALWAYS_RETURN_LIST);
                    List<String> jsonData1 = JsonPath.using(conf1).parse(json).read(node.getExpr());
                    Object obj1 = jsonData1.get(0);
                    if (oConvertUtils.isNotEmpty(obj1)) {
                        // 替换JSON,下一步可能继续取值
                        json = JSON.toJSONString(obj1);
                    }
                    // 也可能作为一个json串返回
                    result = json;
                    log.info(json);
                    break;
                case VALUE:
                    log.info("执行VALUE表达式");
                    Configuration conf2 = conf.addOptions(Option.ALWAYS_RETURN_LIST);
                    List<String> jsonData2 = JsonPath.using(conf2).parse(json).read(node.getExpr());
                    Object obj2 = jsonData2.get(0);
                    // 判断类型,json数据只有string,int,boolean这几个类型
                    if (oConvertUtils.isNotEmpty(obj2)) {
                        // 因为取值只需要String类型,所以不再判断类型,统一强转为string
                        result = String.valueOf(obj2);
//                        if (obj2 instanceof String) {
//                            String resStr = (String) obj2;
//                        } else if (obj2 instanceof Integer) {
//                            Integer resInt = (Integer) obj2;
//                        } else if (obj2 instanceof Boolean) {
//                            Boolean resBool = (Boolean) obj2;
//                        } else if (obj2 instanceof Float) {
//                            Double resDoub = (Double) obj2;
//                        } else if (obj2 instanceof Double) {
//                            Double resDoub = (Double) obj2;
//                        }
                    } else {
                        result = null;
                    }
                    break;
                case SET:
                    log.info("执行VALUE表达式");
                    // 按照表达式的值进行配置,但是需要处理表达式中包含的变量
                    result = node.getExpr();
                    break;
                case REGEX:
                    log.info("执行REGEX表达式");
                    String content = json;
                    if (time == 0) {
                        // 如果第一次就执行正则,则直接从json中进行匹配
                        content = json;
                    } else {
                        // 否则从其他表达式结果result中进行匹配
                        content = result;
                    }
                    if (oConvertUtils.isNotEmpty(content)) {
                        Matcher matcher = Pattern.compile(node.getExpr()).matcher(content);
                        if (matcher.find()) {
                            result = matcher.group(1);
                            log.info("regex : {}", result);
                        } else {
                            log.info("regex not find");
                            result = null;
                        }
                    } else {
                        result = null;
                    }
                    break;
                case PLUGIN:
                    log.info("执行PLUGIN表达式");
                    // 取出表达式空格
                    String pluginExpr = node.getExpr().replace(" ", "");
                    log.info("pluginExpr : {}", pluginExpr);
                    // 进一步解析表达式,取出plugin名称
                    String pluginNameReg = "([a-zA-Z0-9]*?)\\(";
                    Matcher matcher1 = Pattern.compile(pluginNameReg).matcher(pluginExpr);
                    String pluginName = "null";
                    if (matcher1.find()) {
                        pluginName = matcher1.group(1);
                        log.info("pluginName regex : {}", pluginName);
                    } else {
                        log.info("pluginName regex not find");
                    }
                    // 取出变量表达式
                    String paramNameReg = "\\(([a-zA-Z0-9_,\\$]*?)\\)";
                    Matcher matcher2 = Pattern.compile(paramNameReg).matcher(pluginExpr);
                    String paramNameStr = "null";
                    if (matcher2.find()) {
                        paramNameStr = matcher2.group(1);
                        log.info("paramName regex : {}", paramNameStr);
                    } else {
                        log.info("paramName regex not find");
                    }
                    // 取出变量名称
                    // 按“,”分割,取出变量名称
                    String[] paramNameArray = paramNameStr.split(",");
                    for (int i = 0; i < paramNameArray.length; i++) {
                        // 处理变量名称
                        paramNameArray[i] = paramNameArray[i].replace("$", "");
                        // 检查变量是否存在
                        if (!paramPool.containsKey(paramNameArray[i])){
                            log.error("参数 {} 不存在", paramNameArray[i]);
                            throw new RuntimeException("plugin 表达式参数: $" + paramNameArray[i] + " 不存在");
                        } else {
                            log.info("paramName: {}, paramValue: {}", paramNameArray[i], paramPool.get(paramNameArray[i]));
                        }
                    }
                    // 动态调用plugin去执行
                    try {
                        log.info("pluginUrl: {}", pluginUrl);
                        PluginInterface plugin = pluginUtil.loadPlugin(pluginUrl, pluginName);
                        result = pluginUtil.run(plugin, paramNameArray);
                    } catch (Exception e) {
                        throw e;
                    }
                    break;
                case FUNC:
                    result = funcHandle(paramName, node.getExpr());
                    break;
                case ADDITIONAL:
                    break;
                default:
                    throw new RuntimeException("actuator 不支持的表达式类型: " + node.getType() + "=" + node.getExpr());
            }

            time++;
        }
        // 写入paramPool
        if (isAddParamPool) {
            addParamPool(paramName, result);
        }
        return result;
    }

    /**
     * 执行func表达式
     * 1.incr(param) param为自增初始值,需要整形
     * 2.once(param1, param2) 值初始化一次的方法,第一次初始化时使用param1,后续执行表达式使用param2
     *
     */
    private String funcHandle(String paramName, String expr) {
        String result = null;
        // 解析func名称
        String funcNameRegex = "(^[a-zA-Z0-9_]*?)\\(";
        Pattern pattern1 = Pattern.compile(funcNameRegex);
        Matcher matcher1 = pattern1.matcher(expr);
        String funcName = null;
        // 解析func参数表达式
        String paramExprRegex = "\\(([a-zA-Z0-9,\\s]*?)\\)";
        Pattern pattern2 = Pattern.compile(paramExprRegex);
        Matcher matcher2 = pattern2.matcher(expr);
        List<String> paramExprList = new ArrayList<>();
        if (matcher2.find()) {
            String paramExpr = matcher2.group(1);
            // 去掉表达式中所有空格
            paramExpr = paramExpr.replace(" ", "");
            // ","分割,取出所有参数的值
            String[] paramExprArr = paramExpr.split(",");
            paramExprList = Arrays.asList(paramExprArr);
        }

        if (matcher1.find()) {
            funcName = matcher1.group(1);
            // 执行func命令
            switch (funcName) {
                case "incr":
                    // 自增指令
                    // 获取变量值,自增指令只接受一个初始值
                    if (paramExprList.size() < 1) {
                        throw new RuntimeException("func表达式incr需要一个初始参数");
                    }
                    // 由于自增需要不断累加,判断变量池中是否已经存在同名自增变量,如果已经存在则直接取出值,在已有值的基础上自增,如果没有,则按照初始值进行自增
                    if (paramPool.containsKey(paramName)) {
                        // 取出已有值
                        String incrInit = paramPool.get(paramName);
                        // 将string转为int
                        Integer incrValue = Integer.parseInt(incrInit);
                        incrValue++;
                        // 转为string返回
                        result = String.valueOf(incrValue);
                    } else {
                        // 如果不存在这个值则直接使用函数参数进行初始化,首次不执行自增
                        result = paramExprList.get(0);
                    }
                    break;
                case "once":
                    if (paramExprList.size() < 1) {
                        throw new RuntimeException("func表达式once至少需要一个初始参数");
                    }
                    // 如果变量池中没有此变量,则使用第一个参数进行初始化,如果已有变量则使用第二个参数
                    if (!paramPool.containsKey(paramName)) {
                        // 如果变量池中没有此变量,则使用第一个参数进行初始化
                        result = paramExprList.get(0);
                    } else {
                        // 如果已有变量,则使用第二个参数作为结果
                        if (paramExprList.size() < 2) {
                            throw new RuntimeException("func表达式once初始化后续执行时找不到第二个参数");
                        }
                        result = paramExprList.get(1);
                    }
                    break;
                default:
                    break;
            }
        } else {
            throw new RuntimeException("func表达式名称错误,找不到func name");
        }
        return result;
    }

    @Data
    public class ResultCustomParamNode {
        String name;
        String value;
    }

    /**
     * 解析从请求结果中抽取的自定义参数
     *
     * name(自定义变量名): $someName
     * value(表达式): json=xxxxxxx${someName}xxxxx
     */
    public void parseResultCustomParam(String json, JSONArray resultCustomParam) throws Exception {
        log.info("解析从请求结果中抽取的自定义参数");
        Iterator<Object> it = resultCustomParam.iterator();
        // 遍历数组取出变量名及对应表达式
        while (it.hasNext()) {
            JSONObject obj = (JSONObject)it.next();
            ResultCustomParamNode resultCustomParamNode = new ResultCustomParamNode();
            resultCustomParamNode.setName(obj.getString("name"));
            resultCustomParamNode.setValue(obj.getString("value"));
            // 变量名称必须以"$"开头
            if ( oConvertUtils.isNotEmpty(resultCustomParamNode.getName()) && oConvertUtils.isNotEmpty(resultCustomParamNode.getValue()) && resultCustomParamNode.getName().startsWith("$") ) {
                log.info("配置: name: {}, value: {}", resultCustomParamNode.getName(), resultCustomParamNode.getValue());
                // 处理name,去掉"$"符号
                String paramName = resultCustomParamNode.getName().replace("$", "");
                // 解析表达式
                LinkedList<Node> nodeList = parser(resultCustomParamNode.getValue());
                String expResult = actuator(nodeList, json, paramName, true);
                log.info("结果: name: {}, value: {}", resultCustomParamNode.getName(), expResult);
            }
        }
    }

    /**
     * 解析自定义参数
     * $param=xxxxxxxx${param1}xxxxxxx
     */
    public void parseCustomParam(String value) throws Exception {
        if (oConvertUtils.isEmpty(value)) {
            return;
        }
        // 按\n拆分语句
        List<String> statementList = Arrays.stream(value.split("\n")).collect(Collectors.toList());
        for (String statement : statementList) {
            String[] exprArray = statement.split("=", 2);
            String paramName = exprArray[0].replace("$", "");
            // 解析表达式
            LinkedList<Node> nodeList = parser(exprArray[1]);
            String result = actuator(nodeList, null, paramName, true);
            log.info("parseCustomParam: name={}, value={}", paramName, result);
        }
    }

    /**
     * 解析 ${param} 格式变量占位符,并替换
     *
     * @param value 要解析的字符串
     */
    public String parseParamPlaceholder(String value) {
        // 先判断是否包含$param格式的变量
        log.info("parseParam: {}", value);
        Pattern pattern = Pattern.compile(paramNameRegex);
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            // 遍历每个变量名
            // 占位符
            String symbol = matcher.group(0);
            log.info("parseParam symbol: {}", symbol);
            // 变量名
            String name = matcher.group(1).trim();
            log.info("parseParam name: {}", name);
            // 去变量池中去匹配是否已经有这个参数
            if (paramPool.containsKey(name)) {
                // 如果存在则进行替换
                value = value.replace(symbol, paramPool.get(name));
                log.info("paramPool find key: {}, ={}", name, paramPool.get(name));
            } else {
                // 如果不存在则把占位符去除,留空
                value = value.replace(symbol, "");
                log.info("paramPool not find key: {}", name);
            }
            log.info("parseParam result: {}", value);
        }
        return value;
    }

    /**
     * 把自定义变量加入参数池
     * 
     * @param name 自定义变量名
     * @param value 变量值
     */
    public void addParamPool(String name, String value) {
        log.info("加入变量池: name:{}, value: {}", name, value);
        paramPool.put(name.trim(), value);
    }
}
