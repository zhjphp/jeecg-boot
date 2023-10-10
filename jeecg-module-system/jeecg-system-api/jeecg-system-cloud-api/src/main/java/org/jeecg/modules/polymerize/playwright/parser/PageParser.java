package org.jeecg.modules.polymerize.playwright.parser;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @description: 页面解析器
 * @author: wayne
 * @date 2023/8/10 15:07
 *
 * TODO
 * 1.可集成ApiParser中的插件机制
 */
@Data
@Component
@Slf4j
public class PageParser {

    /**匹配表达式类型前缀*/
    public enum LocatorType {

        /**xpath表达式*/
        XPATH("xpath"),

        /**指定xpath元素索要获取的attribute*/
        ATTRIBUTE("attr"),

        /**regex表达式*/
        REGEX("regex"),

        /**直接设定写死值*/
        SET("set"),

        /**附加表达式*/
        ADDITIONAL("addi");

        public final String prefix;

        LocatorType(String prefix) {
            this.prefix = prefix;
        }

    }

    @Data
    public class Node {
        private LocatorType type;
        private String expr;
    }

    /**textContent超时时间配置*/
    public Locator.TextContentOptions textContentOptions;

    /**getAttribute超时时间配置*/
    public Locator.GetAttributeOptions getAttributeOptions;

    /**innerHTML超时时间配置*/
    public Locator.InnerHTMLOptions innerHTMLOptions;

    /**waitForSelector超时时间配置*/
    private Page.WaitForSelectorOptions waitForSelectorOptions;

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
            Node node = new Node();
            String tmpMatch = matchArray[i].trim();
            if (tmpMatch.startsWith(LocatorType.XPATH.prefix)) {
                // playwright支持 “xpath=” 前缀,不需要取去除
                // String expr = tmpMatch.replace(LocatorType.XPATH.prefix + "=", "");
                String expr = tmpMatch;
                node.setType(LocatorType.XPATH);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(LocatorType.ATTRIBUTE.prefix)) {
                String expr = tmpMatch.replace(LocatorType.ATTRIBUTE.prefix + "=", "");
                node.setType(LocatorType.ATTRIBUTE);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(LocatorType.REGEX.prefix)) {
                String expr = tmpMatch.replace(LocatorType.REGEX.prefix + "=", "");
                node.setType(LocatorType.REGEX);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(LocatorType.SET.prefix)) {
                String expr = tmpMatch.replace(LocatorType.SET.prefix + "=", "");
                node.setType(LocatorType.SET);
                node.setExpr(expr.trim());
            } else if (tmpMatch.startsWith(LocatorType.ADDITIONAL.prefix)) {
                String expr = tmpMatch.replace(LocatorType.ADDITIONAL.prefix + "=", "");
                node.setType(LocatorType.ADDITIONAL);
                node.setExpr(expr.trim());
            } else {
                throw new RuntimeException("不支持的表达式类型: " + tmpMatch);
            }
            nodeList.add(node);
        }

        log.info("LinkedList 结构转换完成: {}", nodeList.toString());
        return nodeList;
    }

    /**
     * 等待页面某个Locator定位器加载完成, 用来应对页面中某个部分加载较慢的情况
     *
     * @param match 定位器匹配规则
     * @param page 页面
     */
    public void waitForSelector(String match, Page page) {
        LinkedList<Node> nodeList = parser(match);
        // 由于可能有多次locator定位,或者其他类型表达式,需要过滤一下,并取第一层xpath表达式执行waitForSelector
        for (Node node : nodeList) {
            if (node.getExpr().startsWith(LocatorType.XPATH.prefix)) {
                log.info("waitForSelector 等待页面加载标志: {}", node.getExpr());
                page.waitForSelector(node.getExpr(), waitForSelectorOptions);
                break;
            }
        }
    }

    /**
     * 列表页区块定位器解析
     *
     * @param match 定位器匹配规则
     * @param locator 定位器
     * @param page 页面
     * @param name 匹配规则名称(预留参数,后期用来区分处理各种特殊规则)
     */
    public Locator listPageLocatorParser(String match, Locator locator, Page page, String name) throws RuntimeException {
        log.info("listLocatorMatch filed: {}", name);
        LinkedList<Node> nodeList = parser(match);
        return locatorActuator(nodeList, locator, page);
    }

//    public List<Locator> listPageListLocatorParser(String match, Locator locator, Page page, String name) throws RuntimeException {
//        log.info("listPageLocatorListParser filed: {}", name);
//        LinkedList<Node> nodeList = parser(match);
//        if (nodeList.size() > 1) {
//            throw new RuntimeException("区块定位只支持单指令");
//        }
//        Node node = nodeList.pop();
//        List<Locator> result = new LinkedList<Locator>();
//        if (oConvertUtils.isNotEmpty(locator)) {
//            result = locator.locator(match).all();
//        } else if (oConvertUtils.isNotEmpty(page)) {
//            result = page.locator(match).all();
//        }
//        return result;
//    }

    /**
     * 列表页内容定位器解析
     *
     * @param match 定位器匹配规则
     * @param locator 定位器
     * @param page 页面
     * @param name 匹配规则名称(预留参数,后期用来区分处理各种特殊规则)
     */
    public String listPageContentParser(String match, Locator locator, Page page, String content, String name) throws RuntimeException {
        log.info("listContentMatch filed: {}", name);
        return contentActuator(parser(match), locator, page, content);
    }

    /**
     * 详情页区块定位器解析
     *
     * @param match 定位器匹配规则
     * @param locator 定位器
     * @param page 页面
     * @param name 匹配规则名称(预留参数,后期用来区分处理各种特殊规则)
     */
    public Locator articlePageLocatorParser(String match, Locator locator, Page page, String name) throws RuntimeException {
        log.info("articleLocatorMatch filed: {}", name);
        LinkedList<Node> nodeList = parser(match);
        return locatorActuator(nodeList, locator, page);
    }

    /**
     * 详情页内容定位器解析
     *
     * @param match 定位器匹配规则
     * @param locator 定位器
     * @param page 页面
     * @param name 匹配规则名称(预留参数,后期用来区分处理各种特殊规则)
     */
    public String articlePageContentParser(String match, Locator locator, Page page, String content, String name) throws RuntimeException {
        log.info("articleContentMatch filed: {}", name);
        return contentActuator(parser(match), locator, page, content);
    }

    /**
     * 区块定位器解析
     *
     * @param nodeList 表达式组
     * @param locator 定位器
     * @param page 页面
     */
    private Locator locatorActuator(LinkedList<Node> nodeList, Locator locator, Page page) throws RuntimeException {
        // 需要处理的定位器
        Locator localLocator = locator;
        log.info("开始执行 listLocatorActuator");

        while (nodeList.size() > 0) {
            // 先进先出,取出 node
            Node node = nodeList.pop();
            if (oConvertUtils.isEmpty(node.getExpr())) {
                // 如果表达式为空,则进入下一条表达式
                continue;
            }
            // 执行node
            switch (node.getType()) {
                case XPATH:
                    log.info("执行xpath表达式");
                    if ( oConvertUtils.isNotEmpty(localLocator) ) {
                        // 由于page传入后就不会改变了,所以想实现多次的locator定位,就必须指定了locator,优先使用locator
                        localLocator = localLocator.locator(node.getExpr());
                    } else if (oConvertUtils.isNotEmpty(page)){
                        // 其次如果指定了page
                        localLocator = page.locator(node.getExpr());
                    } else {
                        throw new RuntimeException("locatorMatch 没有指定 locator 或 page");
                    }
                    break;
                default:
                    throw new RuntimeException("locatorMatch 不支持的表达式类型: " + node.getType() + "=" + node.getExpr());
            }
        }

        return localLocator;
    }

    /**
     * 内容定位器解析
     *
     * @param nodeList 表达式组
     * @param locator 定位器
     * @param page 页面
     * @param content 内容
     */
    private String contentActuator(
            LinkedList<Node> nodeList,
            Locator locator,
            Page page,
            String content
    ) throws RuntimeException {
        // 需要处理的定位器
        Locator localLocator = locator;
        // 需要处理的文本内容
        String localContent = content;
        // 返回结果
        String result = null;

        log.info("开始执行 contentActuator");
        while (nodeList.size() > 0) {
            // 先进先出,取出 node
            Node node = nodeList.pop();
            if (oConvertUtils.isEmpty(node.getExpr())) {
                // 如果表达式为空,则进入下一条表达式
                continue;
            }
            // 执行node
            switch (node.getType()) {
                case XPATH:
                    log.info("执行xpath表达式");
                    if ( oConvertUtils.isNotEmpty(localLocator) ) {
                        // 由于page传入后就不会改变了,所以想实现多次的locator定位,就必须指定了locator,优先使用locator
                        localLocator = localLocator.locator(node.getExpr());
                    } else if (oConvertUtils.isNotEmpty(page)){
                        // 其次如果指定了page
                        localLocator = page.locator(node.getExpr());
                    } else {
                        throw new RuntimeException("contentActuator 没有指定 locator 或 page");
                    }
                    break;
                case ATTRIBUTE:
                    log.info("执行property表达式");
                    if (oConvertUtils.isNotEmpty(localLocator)) {
                        if (node.getExpr().equals("text")) {
                            /*
                             *  有时候会出现 localLocator 定位多个元素的问题,目前采用第一种方法
                             *  1. 直接使用textContent或者getAttribute取值,这样遇到多个元素会抛错,方便及时发现及修改
                             *  2. 判断元素个数,然后选择合并或者抛错 (此方法不支持多次xpath定位)
                             */
                            result = localLocator.textContent(textContentOptions);
                        } else if (node.getExpr().equals("html")) {
                            result = localLocator.innerHTML(innerHTMLOptions);
                        } else {
                            result = localLocator.getAttribute(node.getExpr(), getAttributeOptions);
                        }
                    }

                    break;
                case REGEX:
                    log.info("执行regex表达式, regex: {}", node.getExpr());
                    // REGEX表达式 是基于 localContent 文本进行处理的
                    if (oConvertUtils.isNotEmpty(localContent)) {
                        Matcher matcher = Pattern.compile(node.getExpr()).matcher(localContent);
                        if (matcher.find()) {
                            result = matcher.group(1);
                            log.info("regex : {}", result);
                        } else {
                            log.info("regex not find");
                            result = null;
                        }
                    } else {
                        log.warn("regex localContent 为 null, 跳过执行");
                    }
                    break;
                case SET:
                    log.info("执行set表达式, set: {}", node.getExpr());
                    result = node.getExpr();
                    break;
                case ADDITIONAL:
                    // TODO 附加表达式处理程序
                    log.info("执行additional表达式, additional: {}", node.getExpr());
                    break;
                default:
                    throw new RuntimeException("contentActuator 不支持的表达式类型: " + node.getType() + "=" + node.getExpr());
            }

            log.info("执行结果,result: {}", result);
            // 为后续文本处理,更新文本内容
            localContent = result;
        }

        return result;
    }

}
