package org.jeecg.modules.polymerize.playwright;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.URLUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.drawflow.*;
import org.jeecg.modules.polymerize.drawflow.model.*;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import org.jeecg.modules.polymerize.playwright.data.DataStorageService;
import org.jeecg.modules.polymerize.playwright.filter.PlaywrightDataFilter;
import org.jeecg.modules.polymerize.playwright.parser.PageParser;
import org.jeecg.modules.polymerize.playwright.ua.util.FakeUa;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.log.OmsLogger;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Playwright 爬虫
 * 多线程使用说明: https://playwright.dev/java/docs/multithreading
 * listPage.waitForLoadState(LoadState.DOMCONTENTLOADED) 参考 https://playwright.dev/java/docs/actionability
 *
 *
 * @version 1.0
 * @description: Playwright爬虫
 * @author: wayne
 * @date 2023/6/8 17:52
 * TODO
 * 1. 如果找不到总页数, 如何才能到最后一页 (暂时通过指定总页数解决) （增加总稿件数量与每页数量共同计算的方法）
 * 2. xpath与regex联合使用
 * 3. 配置所有xpath的locator超时时间,默认时间为30秒,有点长 (完成)
 * 4. 一个列表,多种详情页面,多套详情规则 (完成)
 * 5. 杀死任务时可以释放资源 (析构方法)(完成)
 * 6. 可以通过job控制台杀死任务 (通过对playwright的TimeoutError与net::ERR_NAME_NOT_RESOLVED错误类型判断,进行选择性抛出与重试) （完成）
 * 7. job系统的日志对接 （完成）
 * 8. 优化列表起始链接的显示
 * 9. 每个采集的字段都要在处理前做过滤与非空判断
 * 10. 重试恢复原有页面时，使用参数传递，不能通过page.url()获取原有链接
 * 11. 自定义locator.attribute提取属性
 */
@Slf4j
@RefreshScope
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlaywrightCrawl {

    private Playwright playwright;

    @Value("${polymerize.playwright.enableHeadless}")
    private boolean enableHeadless;

    @Value("${polymerize.playwright.browserType}")
    private String browserType;

    private Browser browser;

    /**代理IP接口*/
    @Value("${polymerize.ipProxyApi}")
    private String ipProxyApi = "http://localhost:9999/proxy/getOne?type=2";

    /**是否启用代理IP*/
    @Value("${polymerize.playwright.enableIPProxy}")
    private boolean enableIPProxy = false;

    /**请求重试次数*/
    @Value("${polymerize.playwright.retryTimes}")
    private int retryTimes;

    /**预计置顶贴数量*/
    @Value("${polymerize.playwright.toppingCount}")
    private int toppingCount;

    @Value("${polymerize.playwright.sleepTime}")
    private long sleepTime;

    @Value("${polymerize.playwright.listPageScrollPageCount}")
    private int listPageScrollPageCount;

    @Value("${polymerize.playwright.articlePageScrollPageCount}")
    private int articlePageScrollPageCount;

    @Value("${polymerize.playwright.disableLoadResource}")
    private String disableLoadResource;

    private BrowserContext browserContext;

    /**列表页面*/
    private Page listPage;

    /**详情页面*/
    private Page articlePage;

    private Drawflow drawflow;

    @Resource
    DataStorageService dataStorageService;

    /**Locator超时时间配置*/
    @Value("${polymerize.playwright.locatorTimeout}")
    private double locatorTimeout;

    /**page.navigat超时时间配置*/
    @Value("${polymerize.playwright.pageNavigateTimeout}")
    private double pageNavigateTimeout;

    /**page.waitForSelectorOptions超时时间配置*/
    @Value("${polymerize.playwright.pageNavigateTimeout}")
    private double waitForSelectorOptionsTimeout;

    /**Locator超时时间配置*/
    private Locator.TextContentOptions textContentOptions;

    /**Locator超时时间配置*/
    private Locator.InnerHTMLOptions innerHTMLOptions;

    /**Locator超时时间配置*/
    private Locator.GetAttributeOptions getAttributeOptions;

    /**page.navigate超时时间配置*/
    private Page.NavigateOptions navigateOptions;

    /**page.waitForSelector超时时间*/
    private Page.WaitForSelectorOptions waitForSelectorOptions;

    private String informationSourceId;

    private String taskId;

    private String jobId;

    private String informationSourceName;

    private String informationSourceDomain;

    private OmsLogger omsLogger;

    @Resource
    private PageParser pageParser;


    /**
     * 执行爬取任务
     */
    public void run(
            String jsonConfig,
            String informationSourceId,
            String taskId,
            String jobId,
            String informationSourceDomain,
            String informationSourceName,
            OmsLogger omsLogger
    ) throws Exception {
        // 执行爬虫
        try {
            this.informationSourceId = informationSourceId;
            this.taskId = taskId;
            this.jobId = jobId;
            this.informationSourceDomain = informationSourceDomain;
            this.informationSourceName = informationSourceName;
            this.omsLogger = omsLogger;
            // 全局配置
            initPlaywright();
            createBrowser();
            // 初始化规则配置
            drawflow = new Drawflow(jsonConfig);
            log.info("初始化规则配置: {}", drawflow.toString());
            omsLogger.info(logThreadId() + "初始化规则配置: {}", drawflow.toString());
            // 初始化定位器超时时间
            textContentOptions = new Locator.TextContentOptions().setTimeout(locatorTimeout);
            innerHTMLOptions = new Locator.InnerHTMLOptions().setTimeout(locatorTimeout);
            getAttributeOptions = new Locator.GetAttributeOptions().setTimeout(locatorTimeout);
            navigateOptions = new Page.NavigateOptions().setTimeout(pageNavigateTimeout);
            waitForSelectorOptions = new Page.WaitForSelectorOptions().setTimeout(waitForSelectorOptionsTimeout);
            // 初始化pageParser配置
            pageParser.setTextContentOptions(textContentOptions);
            pageParser.setGetAttributeOptions(getAttributeOptions);
            pageParser.setInnerHTMLOptions(innerHTMLOptions);
            pageParser.setWaitForSelectorOptions(waitForSelectorOptions);

            // 迭代所有的起始节点
            log.info("开始遍历StartNode:");
            while (drawflow.hasNext()) {
                DrawflowNode startNode = drawflow.next();
                StartRuleNode startRuleNode = new StartRuleNode(startNode.getData());
                // 如果起始节点配置了要屏蔽的资源,则覆盖默认的ncaos中的骗配置
                if (oConvertUtils.isNotEmpty(startRuleNode.getDisableLoadResource())) {
                    this.disableLoadResource = startRuleNode.getDisableLoadResource();
                }
                omsLogger.info("web资源加载配置: {}", this.disableLoadResource);
                createPage(false);
                log.info("StartNode: {}", startNode.toString());
                // 每个起始节点代表一条线
                // 获取下级节点
                if (startNode.hasChild) {
                    recursion(startNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (oConvertUtils.isNotEmpty(listPage)) {
                log.info("关闭 listPage");
                omsLogger.info(logThreadId() + "关闭 listPage");
                listPage.close();
            }
            if (oConvertUtils.isNotEmpty(articlePage)) {
                log.info("关闭 articlePage");
                omsLogger.info(logThreadId() + "关闭 articlePage");
                articlePage.close();
            }
            if (oConvertUtils.isNotEmpty(browserContext)) {
                log.info("关闭 browserContext");
                omsLogger.info(logThreadId() + "关闭 browserContext");
                browserContext.close();
            }
            browser.close();
            log.info("关闭 browser");
            omsLogger.info(logThreadId() + "关闭 browser");
            playwright.close();
            log.info("关闭 playwright");
            omsLogger.info(logThreadId() + "关闭 playwright");
        }
    }

    /**
     * 初始化 playwright
     */
    private void initPlaywright() {
        playwright = Playwright.create();
    }

    /**
     * 初始化 browser
     */
    private void createBrowser() {
        switch (browserType) {
            case "chromium" :
                browser = playwright.chromium().launch(
                        new BrowserType.LaunchOptions().setHeadless(enableHeadless)
                );
                break;
            case "firefox" :
                browser = playwright.firefox().launch(
                        new BrowserType.LaunchOptions().setHeadless(enableHeadless)
                );
                break;
            case "webkit" :
                browser = playwright.webkit().launch(
                        new BrowserType.LaunchOptions().setHeadless(enableHeadless)
                );
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * 配置browserContext,建立浏览器page
     *
     * @param restoreOriginalPage
     * @throws Exception
     */
    public void createPage(boolean restoreOriginalPage) throws Exception {
        log.info("执行createPage()");
        omsLogger.info(logThreadId() + "执行createPage()");
        // 已经打开的页面
        String currentListPageUrl = null;
        String currentArticlePageUrl = null;
        Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setJavaScriptEnabled(true);
        // 配置ua
        String userAgent = getUserAgent();
        log.info("使用userAgent: {}", userAgent);
        omsLogger.info(logThreadId() + "使用userAgent: {}", userAgent);
        newContextOptions.setUserAgent(userAgent);
        // 配置代理ip
        if (enableIPProxy) {
            String proxyIP = getProxyIP();
            if (oConvertUtils.isNotEmpty(proxyIP)) {
                log.info("使用代理IP: {}", proxyIP);
                omsLogger.info(logThreadId() + "使用代理IP: {}", proxyIP);
                newContextOptions.setProxy(proxyIP);
            }
        }
        // 建立新页面前先关闭
        if (oConvertUtils.isNotEmpty(listPage)) {
            currentListPageUrl = listPage.url();
            listPage.close();
            log.info("关闭原有listPage: {}", currentListPageUrl);
            omsLogger.info(logThreadId() + "关闭原有listPage: {}", currentListPageUrl);
        }
        if (oConvertUtils.isNotEmpty(articlePage)) {
            currentArticlePageUrl = articlePage.url();
            articlePage.close();
            log.info("关闭原有articlePage: {}", currentArticlePageUrl);
            omsLogger.info(logThreadId() + "关闭原有articlePage: {}", currentArticlePageUrl);
        }
        if (oConvertUtils.isNotEmpty(browserContext)) {
            browserContext.close();
            log.info("关闭原有browserContext");
            omsLogger.info(logThreadId() + "关闭原有browserContext");
        }
        browserContext = browser.newContext(newContextOptions);
        // 屏蔽部分资源的加载
        // browserContext.route(disableLoadResource, route -> route.abort());
        browserContext.route(Pattern.compile(disableLoadResource), route -> route.abort());
        // 列表页
        listPage = browserContext.newPage();
        // 隐藏webdriver特征
        listPage.addInitScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");
        if (restoreOriginalPage) {
            log.info("准备恢复原有listPage页面: {}", currentListPageUrl);
            omsLogger.info(logThreadId() + "准备恢复原有listPage页面: {}", currentListPageUrl);
            // 恢复原有页面
            if (oConvertUtils.isNotEmpty(currentListPageUrl)) {
                listPage.navigate(currentListPageUrl, navigateOptions);
                waitForPageLoaded(listPage);
                log.info("恢复原有listPage: {}", currentListPageUrl);
                omsLogger.info(logThreadId() + "恢复原有listPage: {}", currentListPageUrl);
            }
        }
        // 详情页
        articlePage = browserContext.newPage();
        // 隐藏webdriver特征
        articlePage.addInitScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");
        if (restoreOriginalPage) {
            log.info("准备恢复原有articlePage页面: {}", currentArticlePageUrl);
            omsLogger.info(logThreadId() + "准备恢复原有articlePage页面: {}", currentArticlePageUrl);
            // 恢复原有页面
            if (oConvertUtils.isNotEmpty(currentArticlePageUrl)) {
                articlePage.navigate(currentArticlePageUrl, navigateOptions);
                waitForPageLoaded(articlePage);
                log.info("恢复原有articlePage: {}", currentArticlePageUrl);
                omsLogger.info(logThreadId() + "恢复原有articlePage: {}", currentArticlePageUrl);
            }
        }
    }

    /**
     * 递归获取配置节点
     *
     * @param node
     * @throws Exception
     */
    public void recursion(DrawflowNode node) throws Exception {
        log.info("递归执行: {}", node.toString());
        if (node.hasChild) {
            List<String> childIdList = node.getChild();
            for (String childId : childIdList) {
                DrawflowNode childNode = drawflow.getNode(childId);
                // 取出一个节点,就需要去执行
                drawflowNodeProcess(childNode);
                // 执行完成进行当前节点的子节点
                recursion(childNode);
            }
        }
    }

    /**
     * 处理配置节点
     *
     * @param node
     * @throws Exception
     */
    public void drawflowNodeProcess(DrawflowNode node) throws Exception {
        // 列表节点
        if (node.getNodeType().equals(DrawflowNode.LIST_RULE_NODE)) {
            log.info("执行列表节点: {}", node.toString());
            omsLogger.info(logThreadId() + "执行列表节点: {}", node.toString());
            listNodeProcess(node);
        }
        // 稿件节点
        if (node.getNodeType().equals(DrawflowNode.ARTICLE_RULE_NODE)) {
            log.info("执行稿件节点: {}", node.toString());
            omsLogger.info(logThreadId() + "执行稿件节点: {}", node.toString());
            articleNodeProcess(node);
        }
    }

    /**
     * 执行列表节点
     *
     * @param listNode
     * @throws Exception
     */
    public void listNodeProcess(DrawflowNode listNode) throws Exception {
        // 取出需要爬取的起始url
        JSONObject listObj = listNode.getData();
        ListRuleNode listRuleNode = new ListRuleNode(listObj);
        List<String> startUrsList = Arrays.stream(listRuleNode.getStartUrls().split("\n")).collect(Collectors.toList());

        // 取出列表对应的详情节点规则
        List<ArticleRuleNode> articleRuleNodeList = new ArrayList<>();
        List<String> childIdList = listNode.getChild();
        if (childIdList.size() > 0) {
            // 遍历取出对应的详情节点
            for (int i = 0; i < childIdList.size(); i++) {
                // 取出详情节点ID
                String articleNodeId = childIdList.get(i);
                // 取出详情节点
                DrawflowNode articleNode = drawflow.getNode(articleNodeId);
                JSONObject articleObj = articleNode.getData();
                ArticleRuleNode articleRuleNode = new ArticleRuleNode(articleObj);
                articleRuleNodeList.add(articleRuleNode);
            }
        } else {
            // 如果没有定义详情规则,不执行详情节点
        }

        // 开始按url爬取列表页
        for (String startUrl: startUrsList) {
            // 超时重试一次
            int tries = 0;
            while (tries < retryTimes) {
                try {
                    // 打开页面
                    Response response = listPage.navigate(startUrl, navigateOptions);
                    log.info("开始采集列表页: {}", startUrl);
                    omsLogger.info(logThreadId() + "开始采集列表页: {}", startUrl);
                    checkResponse(response, startUrl);
                    // 采集列表
                    getList(listRuleNode, articleRuleNodeList);
                    break;
                } catch (PlaywrightException e) {
                    log.error(listPage.url() + ": " + e.getMessage());
                    omsLogger.error(logThreadId() + listPage.url() + ": " + e.getMessage());
                    if (e.getClass().getName().equals("com.microsoft.playwright.TimeoutError") || e.getMessage().contains("net::ERR_NAME_NOT_RESOLVED") || e.getMessage().contains("net::ERR_CONNECTION_TIMED_OUT")) {
                        tries++;
                        if (tries == retryTimes) {
                            log.error("{},尝试请求 {} 次失败", listPage.url(), tries);
                            omsLogger.error(logThreadId() + "{},尝试请求 {} 次失败", listPage.url(), tries);
                            throw e;
                        }
                        log.info("更换代理IP和UA,尝试重新请求: {}", listPage.url());
                        omsLogger.error(logThreadId() + "更换代理IP和UA,尝试重新请求: {}", listPage.url());
                        // 更换代理IP和UA
                        createPage(true);
                    } else {
                        throw e;
                    }
                }
            }
        }

    }

    private void waitForPageLoaded(Page page) {
        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        } catch (PlaywrightException e) {
            if (e.getClass().getName().equals("com.microsoft.playwright.TimeoutError") || e.getMessage().contains("net::ERR_NAME_NOT_RESOLVED") || e.getMessage().contains("net::ERR_CONNECTION_TIMED_OUT") || e.getMessage().contains("net::ERR_CONNECTION_REFUSED")) {
                log.warn("waitForLoadState 超时: {}, {}", page.url(), e.getMessage());
                omsLogger.warn(logThreadId() + "waitForLoadState 超时: {}, {}", page.url(), e.getMessage());
            } else {

            }

        }
    }

    /**
     * 滚动条到最下方
     * https://stackoverflow.com/questions/69183922/playwright-auto-scroll-to-bottom-of-infinite-scroll-page
     * https://blog.csdn.net/weixin_42152811/article/details/120828564
     * https://github.com/microsoft/playwright/issues/4302
     *
     * @param page
     * @param pageCount
     */
    private void scrollToBottom(Page page, int pageCount) {
        Object scrollHeight = page.evaluate(
                "() => document.documentElement.scrollHeight"
        );
        log.info("document.documentElement.scrollHeight: {}", scrollHeight.toString());
        double y = Double.parseDouble(scrollHeight.toString());
        for (int i = 0 ; i < pageCount; i ++) {
            page.mouse().wheel(0, y);
            waitForPageLoaded(page);
            // page.waitForTimeout(500);
        }
    }

    /**
     * 滚动条到最下方(瀑布流使用)
     * https://stackoverflow.com/questions/69183922/playwright-auto-scroll-to-bottom-of-infinite-scroll-page
     * https://blog.csdn.net/weixin_42152811/article/details/120828564
     * https://github.com/microsoft/playwright/issues/4302
     *
     * @param page
     * @param pageCount
     * @param bottomMatch
     */
    private void waterfallScrollToBottom(Page page, int pageCount, String bottomMatch, String pageMatch, String moreMatch) {
        Object scrollHeight = page.evaluate(
                "() => document.documentElement.scrollHeight"
        );
        log.info("document.documentElement.scrollHeight: {}", scrollHeight.toString());
        double y = Double.parseDouble(scrollHeight.toString());
        log.info("开始执行瀑布流下拉");
        // 如果配置了底部特征
        if (oConvertUtils.isNotEmpty(bottomMatch)) {
            log.info("使用底部元素匹配规则");
            for (int i = 0 ; i < 1000000; i ++) {
                page.mouse().wheel(0, y);
                waitForPageLoaded(page);
                try {
                    // 如果出现底部标识,则不在滚动
                    if (oConvertUtils.isNotEmpty(page.locator(bottomMatch).innerHTML(innerHTMLOptions))) {
                        break;
                    }
                } catch (TimeoutError e) {
                    // 暂时不做处理
                }
            }
        } else if (oConvertUtils.isNotEmpty(pageCount) && pageCount > 0) {
            // 是否定义总下拉屏数
            log.info("使用下拉屏数: {}", pageCount);
            // 如果没有配置底部特征
            for (int i = 0 ; i < pageCount; i ++) {
                page.mouse().wheel(0, 500);
                waitForPageLoaded(page);
                log.info("屏数: {}", i);
                page.waitForTimeout(200);
            }
        } else {
            // 判断区块数量是否有变化
            log.info("使用自动判断是否到底");
            // 每次滚动之前的总条数
            int preCount = 0;
            // 捕获到的总条数
            int totalCount = page.locator(pageMatch).all().size();
            // 保险,防止无限循环
            int insure = 1000000;
            do {
                if (oConvertUtils.isNotEmpty(moreMatch)) {
                    try {
                        Locator moreLocator = page.locator(moreMatch);
                        log.info("检查是否存在查看更多按钮: {}", moreLocator.count());
                        if ( moreLocator.count() == 1) {
                            log.info("点击查看更多按钮");
                            omsLogger.info("点击查看更多按钮");
                            moreLocator.click();
                            waitForPageLoaded(page);
                            page.waitForTimeout(500);
                        }
                    } catch (TimeoutError e) {
                        // 捕获不存在元素的错误
                        log.info("找不到查看更多按钮");
                        omsLogger.info("找不到查看更多按钮");
                    }
                }
                for (int i = 0; i < 6; i++) {
                    log.info("向下滚动页面...");
                    page.mouse().wheel(0, y/2);
                    waitForPageLoaded(page);
                    page.waitForTimeout(2000);
                }
                preCount = totalCount;
                totalCount = page.locator(pageMatch).all().size();
                log.info("totalCount: {}", totalCount);
                log.info("preCount: {}", preCount);
            } while ( (totalCount > preCount) && (insure > 0) );
            log.info("页面已经到底");
            omsLogger.info("页面已经到底");
        }
    }

    /**
     * 列表页内容匹配解析
     *
     * @param match
     * @param locator
     * @param name
     * @return String
     * @throws RuntimeException
     */
    public String listPageContentParser(String match, Locator locator, Page page, String name) throws RuntimeException {
        String result = pageParser.listPageContentParser(match, locator, page, listPage.content(), name);
        return result;
    }

    /**
     * 列表页区块匹配解析
     *
     * @param match
     * @param locator
     * @param name
     * @return String
     * @throws RuntimeException
     */
    public Locator listPageLocatorParser(String match, Locator locator, Page page, String name) throws RuntimeException {
        Locator locatorResult = pageParser.listPageLocatorParser(match, locator, page, name);
        return locatorResult;
    }

    /**
     * 采集列表
     *
     * @param listRuleNode
     * @param articleRuleNodeList
     * @throws Exception
     */
    public void getList(ListRuleNode listRuleNode, List<ArticleRuleNode> articleRuleNodeList) throws Exception {
        // 获取最大翻页深度(总页数),如果没有设定总页数,则默认只取第一页内容
        int totalPage = 1;
        if (oConvertUtils.isNotEmpty(listRuleNode.getPageDepth())) {
            // 优先使用翻页深度配置
            totalPage = listRuleNode.getPageDepth();
            log.info("指定分页深度: {}", totalPage);
        } else if (oConvertUtils.isNotEmpty(listRuleNode.getTotalPageMatch())) {
            // 其次判断总页数匹配
            try {
                String tmpTotalPage = listPageContentParser(listRuleNode.getTotalPageMatch(), null, listPage, RuleNodeUtil.getFiledName(ListRuleNode::getTotalPageMatch));
                totalPage = Integer.parseInt(tmpTotalPage);
            } catch (TimeoutError e) {
                log.warn("没有匹配到总页数,默认只有一页");
                omsLogger.warn("没有匹配到总页数,默认只有一页");
            }
            log.info("指定总页数匹配: {}", totalPage);
        } else if (oConvertUtils.isNotEmpty(listRuleNode.getTotalCountMatch())) {
            log.info("使用总稿件数量匹配");
            // 判断是否配置总稿件数量匹配
            Integer totalCount = Integer.parseInt(listPageContentParser(listRuleNode.getTotalCountMatch(), null, listPage, RuleNodeUtil.getFiledName(ListRuleNode::getTotalCountMatch)));
            log.info("使用总稿件数量为: {}", totalCount);
            if (totalCount != null && totalCount > 0) {
                // 查看是否指定每页稿件数量
                if ( oConvertUtils.isNotEmpty(listRuleNode.getPageCount()) && listRuleNode.getPageCount() > 0 ) {
                    int pageCount = listRuleNode.getPageCount();
                    totalPage = totalCount/pageCount;
                    if (totalCount%pageCount > 0) {
                        totalPage+=1;
                    }
                    log.info("指定每页稿件数量: {}", pageCount);
                } else {
                    // 如果没有指定每页数量,则需要判断每页的稿件数量
                    List<Locator> locators = listPage.locator(listRuleNode.getPageMatch()).all();
                    int pageCount = locators.size();
                    if (pageCount > 0) {
                        totalPage = totalCount/pageCount;
                        if (totalCount%pageCount > 0) {
                            totalPage+=1;
                        }
                        log.info("自动判断每页稿件数量: {}", pageCount);
                    } else {
                        log.warn("自动获取每页稿件数量异常, pageCount: {}", pageCount);
                    }
                }
            }
        }
        log.info("总页数：{}", totalPage);
        omsLogger.info(logThreadId() + "总页数：{}", totalPage);
        // 是否翻页
        boolean isPageDown = true;
        // 当前页码
        int currentPage = 0;
        // 预防置顶稿件
        int preventToppingCount = 0;
        // 判断稿件日期是否符合目标区间,是否翻页继续爬取
        while ( (currentPage < totalPage) && isPageDown ) {
            log.info("当前页序号: {}", currentPage);
            // 一页列表结果
            List<ListResult> resultList = new ArrayList<>();
            // 匹配区块
            log.info("开始匹配当前页列表区块");
            // 等待加载完成
            waitForPageLoaded(listPage);
            // 如果只用waitForLoadState对于页面渲染较慢的网站无法抓取到内容
            try {
                listPage.waitForSelector(listRuleNode.getPageMatch(), waitForSelectorOptions);
            } catch (TimeoutError e) {
                // 此处不做处理,页面继续执行
                log.warn("加载列表页未找到区块定位内容");
            }
            // 滚动条到底
            int scrollCount = listPageScrollPageCount;
            if (listRuleNode.getWaterfallFlag()) {
                // 瀑布流
                waterfallScrollToBottom(
                        listPage, listRuleNode.getWaterfallPageCount(), listRuleNode.getWaterfallBottomMatch(), listRuleNode.getPageMatch(), listRuleNode.getMoreMatch()
                );
            } else {
                scrollToBottom(listPage, scrollCount);
            }
            // 定位列表所有区块
            List<Locator> locators = listPage.locator(listRuleNode.getPageMatch()).all();
            log.info("locators size: {}", locators.size());
            if (locators.size() == 0) {
                throw new Exception("列表第" + currentPage + "页,没有匹配到任何内容");
            }
            // 遍历每个区块
            for (Locator locator: locators) {
                log.info("从列表区块中取出每个条目");
                ListResult listResult = new ListResult();
                // 稿件标题
                String title = null;
                if (oConvertUtils.isNotEmpty(listRuleNode.getArticleTitleMatch())) {
                    title = listPageContentParser(listRuleNode.getArticleTitleMatch(), locator, null, RuleNodeUtil.getFiledName(ListRuleNode::getArticleTitleMatch));
                }
                listResult.setTitle(title);
                log.info("获取标题, title: {}", title);
                // 稿件日期
                String dateStr = null;
                if (oConvertUtils.isNotEmpty(listRuleNode.getArticleDateMatch())) {
                    dateStr = listPageContentParser(listRuleNode.getArticleDateMatch(), locator, null, RuleNodeUtil.getFiledName(ListRuleNode::getArticleDateMatch));
                }
                Date cutDate = null;
                if (oConvertUtils.isNotEmpty(dateStr)) {
                    try {
                        log.info("格式化时间: {}", dateStr);
                        cutDate = DateUtils.cutDate(dateStr);
                        listResult.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cutDate));
                    } catch (RuntimeException e) {
                        log.warn("格式化时间错误: {}", dateStr);
                        omsLogger.warn(logThreadId() + "格式化时间错误: {}", dateStr);
                        listResult.setDate(null);
                    }
                } else {
                    listResult.setDate(null);
                }
                log.info("获取时间, cutDate: {}", cutDate);
                // 稿件链接
                String articleUrl = null;
                if (oConvertUtils.isNotEmpty(listRuleNode.getArticleUrlMatch())) {
                    articleUrl = listPageContentParser(listRuleNode.getArticleUrlMatch(), locator, null, RuleNodeUtil.getFiledName(ListRuleNode::getArticleUrlMatch));
                    // 过滤获取到的URL
                    articleUrl = oConvertUtils.replaceBlank(articleUrl);
                    // 判断取出的url是绝对地址还是相对地址
                    if (oConvertUtils.isNotEmpty(articleUrl)) {
                        // 如果是相对地址,把相对地址转换为绝对地址
                        URI relativeUri = URI.create(articleUrl);
                        URI currentUri = URI.create(listPage.url());
                        URI absoluteUri = currentUri.resolve(relativeUri);
                        articleUrl = absoluteUri.toString();
                    }
                }
                log.info("获取链接, articleUrl: {}", articleUrl);
                listResult.setUrl(articleUrl);
                log.info("列表条目: {}", listResult.toString());
                // 判断取出的url是否是外链
                if ( oConvertUtils.isEmpty(articleUrl)) {
                    // 如果没有取到文章链接则不处理
                    log.warn("无法获取稿件url");
                    omsLogger.warn(logThreadId() + "无法获取稿件url");
                    listErrorDataProcess(listResult.getUrl(), listResult.getTitle(), listResult.getDate(), "无法获取稿件url");
                } else if ( !URLUtils.isSameDomainName(articleUrl, listPage.url()) && !listRuleNode.getEnableOutside() ) {
                    log.info("当前条目为外链内容,不进行采集: {}", listResult.toString());
                    listErrorDataProcess(listResult.getUrl(), listResult.getTitle(), listResult.getDate(), "外链");
                } else {
                    // 判断日期是否为目标时间段的数据
                    if (
                            ( oConvertUtils.isNotEmpty(listRuleNode.getStartTime()) && oConvertUtils.isNotEmpty(cutDate) && cutDate.after(listRuleNode.getStartTime()) )
                    ) {
                        log.info("设定了有效时间段,在StartTime之后");
                        if (
                                ( oConvertUtils.isNotEmpty(listRuleNode.getEndTime()) && oConvertUtils.isNotEmpty(cutDate) && cutDate.before(listRuleNode.getEndTime()) )
                        ) {
                            // 如果设定了起止时间,且在时间段内,则为有效数据
                            log.info("在getEndTime()之前,为有效时间段数据");
                            resultList.add(listResult);
                        } else {
                            log.info("不在getEndTime()之前,继续向前寻找");
                            listErrorDataProcess(listResult.getUrl(), listResult.getTitle(), listResult.getDate(), "不在getEndTime()之前");
                        }
                    } else if (
                            oConvertUtils.isEmpty(listRuleNode.getStartTime())
                                    &&
                                    oConvertUtils.isEmpty(listRuleNode.getEndTime())
                                    &&
                                    (listRuleNode.getEffectiveDays() > 0)
                                    &&
                                    oConvertUtils.isNotEmpty(cutDate)
                                    &&
                                    cutDate.after(
                                            org.apache.commons.lang.time.DateUtils.addDays(new Date(), listRuleNode.getEffectiveDays() * -1)
                                    )
                    ) {
                        // 如果没有设定起止时间,且设定了有效天数,且在有效天数内,则为有效数据
                        log.info("没有设定起止时间,且设定了有效天数,且在有效天数内,为有效数据");
                        resultList.add(listResult);
                    } else if (
                            oConvertUtils.isEmpty(listRuleNode.getStartTime())
                                    &&
                                    oConvertUtils.isEmpty(listRuleNode.getEndTime())
                                    &&
                                    (listRuleNode.getEffectiveDays() == 0)
                    ) {
                        // 如果都没设定,则所有数据为有效
                        log.info("都没设定,所有数据为有效");
                        resultList.add(listResult);
                    } else {
                        // 其他情况,丢弃数据
                        log.info("超出目标时间范围,目标起始时间: {}, 目标结束时间: {}, 目标有效天数: {}, 稿件时间: {}", listRuleNode.getStartTime(), listRuleNode.getStartTime(), listRuleNode.getEffectiveDays(), cutDate);
                        preventToppingCount++;
                        log.info("防止有置顶帖,继续处理,当前处理数量: {}, 配置容忍数量: {}", preventToppingCount, toppingCount);
                        listErrorDataProcess(listResult.getUrl(), listResult.getTitle(), listResult.getDate(), "超出目标时间范围");
                        if (preventToppingCount >= toppingCount) {
                            log.info("到达置顶帖容忍数量,停止翻页");
                            isPageDown = false;
                            break;
                        } else {
                            log.info("不停止翻页");
                        }
                    }
                }

            }
            // 如果定义了详情规则,根据列表爬取详情页面
            if (!oConvertUtils.listIsEmpty(articleRuleNodeList)) {
                for (ListResult result : resultList) {
                    getArticle(result.getUrl(), articleRuleNodeList);
                }
            } else {
                // 没有定义详情规则,不执行详情页面采集
            }
            // 判断是否达到最大页面,或是否允许翻页
            if ( isPageDown && ( (currentPage + 1) < totalPage ) ) {
                // 判断是否设置下一页按钮匹配,如果没有设定,则不执行翻页
                if (oConvertUtils.isEmpty(listRuleNode.getNextMatch())) {
                    log.info("没有设置翻页按钮匹配,列表执行结束,停止翻页");
                    break;
                }
                // 判断下一页按钮是否可用
                Locator nextButton = listPageLocatorParser(listRuleNode.getNextMatch(), null, listPage, RuleNodeUtil.getFiledName(ListRuleNode::getNextMatch));
                if (nextButton.count() == 1) {
                    if (nextButton.isDisabled()) {
                        log.info("下一页按钮禁用,列表执行结束,停止翻页");
                        break;
                    } else {
                        log.info("点击下一页");
                        nextButton.click();
                        currentPage++;
                        listPage.waitForTimeout(sleepTime);
                    }
                } else {
                    log.warn("无法定位下一页按钮,列表执行结束,停止翻页: {}", listPage.url());
                    omsLogger.warn(logThreadId() + "无法定位下一页按钮,列表执行结束,停止翻页", listPage.url());
                    break;
                }
                // Thread.sleep(sleepTime);
            } else {
                log.info("列表执行结束,停止翻页");
                break;
            }
        }
    }

    /**
     * 执行详情节点
     *
     * @param articleNode
     * @throws Exception
     */
    public void articleNodeProcess(DrawflowNode articleNode) throws Exception {
        // 如果是详情节点,则判断是否为单页采集节点
        JSONObject obj = articleNode.getData();
        ArticleRuleNode articleRuleNode = new ArticleRuleNode(obj);
        if (articleRuleNode.getSingleFlag()) {
            try {
                log.info("当前为单页采集节点");
                omsLogger.info(logThreadId() + "当前为单页采集节点");
                List<ArticleRuleNode> articleRuleNodeList = new ArrayList<>();
                articleRuleNodeList.add(articleRuleNode);
                getArticle(articleRuleNode.getSingleUrl(), articleRuleNodeList);
            } catch (Exception e) {
                throw e;
            }
        } else {
            // 非单页采集无法独立执行
            log.warn("无法独立执行非单页规则稿件采集");
            omsLogger.warn(logThreadId() + "无法独立执行非单页规则稿件采集");
        }
    }

    /**
     * 详情页内容匹配解析
     *
     * @param match
     * @param locator
     * @param name
     * @return String
     * @throws RuntimeException
     */
    public String articlePageContentParser(String match, Locator locator, Page page, String name) throws RuntimeException {
        String result = pageParser.articlePageContentParser(match, locator, page, listPage.content(), name);
        return result;
    }

    /**
     * 详情页区块匹配解析
     *
     * @param match
     * @param locator
     * @param name
     * @return String
     * @throws RuntimeException
     */
    public Locator articlePageLocatorParser(String match, Locator locator, Page page, String name) throws RuntimeException {
        Locator locatorResult = pageParser.articlePageLocatorParser(match, locator, page, name);
        return locatorResult;
    }

    /**
     * 采集详情
     *
     * @param articleUrl
     * @param articleRuleNodeList
     * @throws Exception
     */
    public ArticleResult getArticle(String articleUrl, List<ArticleRuleNode> articleRuleNodeList) throws Exception {
        ArticleResult articleResult = new ArticleResult();
        articleResult.setInformationSourceId(informationSourceId);
        articleResult.setTaskId(taskId);
        articleResult.setJobId(jobId);
        articleResult.setInformationSourceDomain(informationSourceDomain);
        articleResult.setInformationSourceName(informationSourceName);

        LinkedList<ArticleRuleNode> articleRuleNodeLink = new LinkedList<ArticleRuleNode>();
        for (ArticleRuleNode listNode : articleRuleNodeList) {
            // 判断当前节点规则是否可用
            if (oConvertUtils.isEmpty(listNode.getRuleSelectByDomainMatch())) {
                // 1. 如果没有配置适用域名为有效
                articleRuleNodeLink.add(listNode);
                log.info("有效规则,没有填写域名适配规则,默认无限制 {}", listNode.toString());
                omsLogger.info("有效规则,没有填写域名适配规则,默认无限制");
            } else {
                // 2. 如果配置了适用域名,则判断是否适用于当前域名
                // 获取当前域名
                String tmpDomain = new URL(articleUrl).getHost();
                if (tmpDomain.matches(listNode.getRuleSelectByDomainMatch())) {
                    // 2.1 适用于当前域名则 push 进入LinkedList,队头存放最适用的规则,通过 pop 优先使用此规则
                    articleRuleNodeLink.push(listNode);
                    log.info("有效规则,规则: {}, 适配当前域名: {}", listNode.getRuleSelectByDomainMatch(), articleUrl);
                    omsLogger.info("有效规则,规则: {}, 适配当前域名: {}", listNode.getRuleSelectByDomainMatch(), articleUrl);
                } else {
                    // 2.1 不适用于当前域名则不使用此规则
                    log.info("无效规则,规则: {}, 不适配当前域名: {}", listNode.getRuleSelectByDomainMatch(), articleUrl);
                    omsLogger.info("无效规则,规则: {}, 不适配当前域名: {}", listNode.getRuleSelectByDomainMatch(), articleUrl);
                }
            }
        }

        log.info("有效规则数量: {}", articleRuleNodeLink.size());
        // 如果一个规则不行,则需要更换另一个规则
        // 是否需要更换节点规则
        boolean changeRule = true;
        while( changeRule && (articleRuleNodeLink.size() > 0) ) {
            try {
                // pop 队头为最适合的规则
                ArticleRuleNode articleRuleNode = articleRuleNodeLink.pop();
                articleResult.setCustomTags(articleRuleNode.getCustomTags());
                // 超时重试一次
                int tries = 0;
                while (tries < retryTimes) {
                    try {
                        log.info("开始采集稿件: {}, 节点规则: {}", articleUrl, articleRuleNode.toString());
                        omsLogger.info(logThreadId() + "开始采集稿件: {}", articleUrl);
                        // 打开页面
                        Response response = articlePage.navigate(articleUrl, navigateOptions);
                        // 查验响应码
                        checkResponse(response, articleUrl);
                        waitForPageLoaded(articlePage);
                        // 如果只用waitForLoadState对于页面渲染较慢的网站无法抓取到内容,采取等待标志性元素探测的方式判断页面是否加载完成,此处使用稿件正文内容进行探测
                        try {
                            pageParser.waitForSelector(articleRuleNode.getContentMatch(), articlePage);
                        } catch (TimeoutError e) {
                            // 此处不做处理,页面继续执行
                            log.warn("加载详情页面未找到详情定位内容, {}", articleUrl);
                            omsLogger.info(logThreadId() + "加载详情页面未找到详情定位内容, {}", articleUrl);
                        }
                        // 滚动条到底
                        scrollToBottom(articlePage, articlePageScrollPageCount);
                        // 判断是否需要点击后查看更多
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getMoreButtonMatch())) {
                            Locator moreButton = articlePageLocatorParser(articleRuleNode.getMoreButtonMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getMoreButtonMatch));
                            moreButton.click();
                            // 等待加载完成
                            waitForPageLoaded(articlePage);
                            // 滚动条到底
                            scrollToBottom(articlePage, articlePageScrollPageCount);
                        }
                        // 匹配内容
                        // 判断是否为单页采集,如果为单页采集,则忽略传入的URL,使用配置中的url
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getSingleFlag()) && articleRuleNode.getSingleFlag()) {
                            articleUrl = articleRuleNode.getSingleUrl();
                        }
                        log.warn("开始匹配规则数据, {}", articleUrl);
                        omsLogger.info(logThreadId() + "开始匹配规则数据, {}", articleUrl);
                        // 按配固定则匹配内容
                        articleResult.setUrl(articleUrl);
                        if (articlePage.locator("//meta[@name='keywords']").count() == 1) {
                            articleResult.setKeywords(articlePage.locator("//meta[@name='keywords']").getAttribute("content", getAttributeOptions));
                        }
                        if (articlePage.locator("//meta[@name='description']").count() == 1) {
                            articleResult.setDescription(articlePage.locator("//meta[@name='description']").getAttribute("content", getAttributeOptions));
                        }
                        // 按配置规则匹配内容
                        // 标题
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getTitleMatch())) {
                            String title = articlePageContentParser(articleRuleNode.getTitleMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getTitleMatch));
                            articleResult.setTitle(title);
                        }
                        // 正文
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getContentMatch())) {
                            String content = articlePageContentParser(articleRuleNode.getContentMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getContentMatch));
                            articleResult.setContent(content);
                        }
                        // 时间
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getDateMatch())) {
                            String date = articlePageContentParser(articleRuleNode.getDateMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getDateMatch));
                            articleResult.setDate(date);
                        }
                        // 栏目
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getTopicMatch())) {
                            String topic = articlePageContentParser(articleRuleNode.getTopicMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getTopicMatch));
                            articleResult.setTopic(topic);
                        }
                        // 子标题(元标签)
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getSubtitleMatch())) {
                            String subtitle = articlePageContentParser(articleRuleNode.getSubtitleMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getSubtitleMatch));
                            articleResult.setSubtitle(subtitle);
                        }
                        // 来源
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getSourceMatch())) {
                            String source = articlePageContentParser(articleRuleNode.getSourceMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getSourceMatch));
                            articleResult.setSource(source);
                        }
                        // 出处
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getReferenceMatch())) {
                            String reference = articlePageContentParser(articleRuleNode.getReferenceMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getReferenceMatch));
                            articleResult.setReference(reference);
                        }
                        // 作者
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getAuthorMatch())) {
                            String author = articlePageContentParser(articleRuleNode.getAuthorMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getAuthorMatch));
                            articleResult.setAuthor(author);
                        }
                        // 访问量
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getVisitMatch())) {
                            String visit = articlePageContentParser(articleRuleNode.getVisitMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getVisitMatch));
                            articleResult.setVisit(visit);
                        }
                        // 评论量
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getCommentMatch())) {
                            String comment = articlePageContentParser(articleRuleNode.getCommentMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getCommentMatch));
                            articleResult.setComment(comment);
                        }
                        // 收藏量
                        if (oConvertUtils.isNotEmpty(articleRuleNode.getCollectMatch())) {
                            String collect = articlePageContentParser(articleRuleNode.getCollectMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getCollectMatch));
                            articleResult.setCollect(collect);
                        }
                        // 没需求,不处理 articleRuleNode.getCustomConfig();
                        // log.info("稿件内容采集完成: \nurl: {} \ntitle: {} \narticleResult: {}", articleResult.getUrl(), articleResult.getTitle(), articleResult.toString());
                        log.info("稿件内容采集完成: \nurl: {} \ntitle: {} ", articleResult.getUrl(), articleResult.getTitle());
                        omsLogger.info(logThreadId() + "稿件内容采集完成: \nurl: {} \ntitle: {} ", articleResult.getUrl(), articleResult.getTitle());
                        // 将数据推入存储处理队列
                        articleDataProcess(articleResult);
                        articlePage.waitForTimeout(sleepTime);
                        changeRule = false;
                        break;
                    } catch (PlaywrightException e) {
                        // 如果捕获PlaywrightException的全部错误,则无法通过job控制台杀死任务,通过对playwright的TimeoutError与net::ERR_NAME_NOT_RESOLVED错误类型判断,进行选择性抛出与重试
                        if (e.getClass().getName().equals("com.microsoft.playwright.TimeoutError") || e.getMessage().contains("net::ERR_NAME_NOT_RESOLVED") || e.getMessage().contains("net::ERR_CONNECTION_TIMED_OUT") || e.getMessage().contains("net::ERR_CONNECTION_REFUSED")) {
                            log.error("{} : {}", articleUrl, e.getMessage());
                            omsLogger.error(logThreadId() + "尝试错误: {} : {}", articleUrl, e.getMessage());
                            tries++;
                            log.error("当前尝试次数: {}", tries);
                            if (tries == retryTimes) {
                                log.error("{},尝试请求 {} 次失败", articleUrl, tries);
                                omsLogger.error(logThreadId() + "{},尝试请求 {} 次失败", articleUrl, tries);
                                throw e;
                            } else {
                                log.warn("更换代理IP和UA,尝试重新请求: {}", articleUrl);
                                omsLogger.warn(logThreadId() + "更换代理IP和UA,尝试重新请求: {}", articleUrl);
                                // 更换代理IP和UA
                                createPage(true);
                            }
                        } else {
                            throw e;
                        }
                    }
                }
                break;
            } catch (PlaywrightException e) {
                log.error("稿件详情采集失败: {}", articleUrl);
                omsLogger.error(logThreadId() + "稿件详情采集失败: {}, {}", articleUrl, e.getMessage());
                log.error("尝试更换详情匹配规则");
                omsLogger.error(logThreadId() + "尝试更换详情匹配规则");
                // 如果捕获PlaywrightException的全部错误,则无法通过job控制台杀死任务,通过对playwright的TimeoutError与net::ERR_NAME_NOT_RESOLVED错误类型判断,进行选择性抛出与重试
                if (e.getClass().getName().equals("com.microsoft.playwright.TimeoutError") || e.getMessage().contains("net::ERR_NAME_NOT_RESOLVED") || e.getMessage().contains("net::ERR_CONNECTION_TIMED_OUT") || e.getMessage().contains("net::ERR_CONNECTION_REFUSED")) {
                    if (articleRuleNodeLink.size() == 0) {
                        log.info("没有更多匹配规则");
                        omsLogger.error("没有更多匹配规则");
                        // 没有下一个规则,则跳出循环
                        changeRule = false;
                        // 记录丢弃数据
                        articleErrorDataProcess(articleResult, e.getMessage());
                        break;
                    } else {
                        // 还有下一个规则,继续执行
                        log.info("更换下一个详情规则: {}", articleUrl);
                        omsLogger.info(logThreadId() + "更换下一个详情规则: {}", articleUrl);
                    }
                } else {
                    articleErrorDataProcess(articleResult, e.getMessage());
                    throw e;
                }
            }
        }
        return articleResult;
    }

    /**
     * 稿件数据处理
     * @param articleResult
     */
    public void articleDataProcess(ArticleResult articleResult) throws Exception {
        log.info("准备处理数据:");
        // 依次处理稿件数据
        // 1.过滤数据
        articleDataFilter(articleResult);
        // 2.处理部分数据
        // log.info("开始处理部分数据:");
        if (oConvertUtils.isNotEmpty(articleResult.getDate())) {
            Date cutDate = DateUtils.cutDate(articleResult.getDate());
            articleResult.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cutDate));
        }
        // 3.存储数据
        log.info("开始存储数据:");
        TmpCrawlData tmpCrawlData = new TmpCrawlData(articleResult);
        boolean result = dataStorageService.addTmpCrawlData(tmpCrawlData);
        if (result) {
            log.info("数据存储成功: ", articleResult.getUrl());
            omsLogger.info(logThreadId() + "数据存储成功: ", articleResult.getUrl());
        } else {
            throw new RuntimeException("数据存储失败: " + articleResult.getUrl());
        }
    }

    /**
     * 列表错误数据处理
     * @param url
     * @param title
     * @param date
     * @param reason
     */
    public void listErrorDataProcess(String url, String title, String date, String reason) throws Exception {
        log.info("准备处理列表错误数据:");

        TmpCrawlData tmpCrawlData = new TmpCrawlData();
        tmpCrawlData.setTitle(title);
        tmpCrawlData.setUrl(url);
        tmpCrawlData.setInformationsourceid(informationSourceId);
        tmpCrawlData.setInformationsourceDomain(informationSourceDomain);
        tmpCrawlData.setInformationsourceName(informationSourceName);
        tmpCrawlData.setTaskid(taskId);
        tmpCrawlData.setJobid(jobId);
        tmpCrawlData.setDate(date);
        tmpCrawlData.setReason(reason);
        // 错误数据
        tmpCrawlData.setErrorCode(2);
        boolean result = dataStorageService.addTmpCrawlData(tmpCrawlData);
        log.info("列表错误数据内容: {}", tmpCrawlData.toString());
        if (result) {
            log.info("列表错误数据存储成功: {}, {}", url, reason);
            omsLogger.info(logThreadId() + "列表错误数据存储成功: {}, {}", url, reason);
        } else {
            throw new RuntimeException("列表错误数据存储失败: " + url);
        }
    }

    /**
     * 稿件错误数据处理
     * @param articleResult
     */
    public void articleErrorDataProcess(ArticleResult articleResult, String reason) throws Exception {
        log.info("准备处理稿件错误数据:");
        // 依次处理稿件数据
        // 1.过滤数据
        articleDataFilter(articleResult);
        // 2.处理部分数据
        // log.info("开始处理部分数据:");
        // 3.存储数据
        log.info("开始存储稿件错误数据:");
        TmpCrawlData tmpCrawlData = new TmpCrawlData(articleResult);
        // 错误数据
        tmpCrawlData.setErrorCode(1);
        tmpCrawlData.setReason(reason);
        boolean result = dataStorageService.addTmpCrawlData(tmpCrawlData);
        log.info("稿件错误数据内容: {}", tmpCrawlData.toString());
        if (result) {
            log.info("稿件错误数据存储成功: {}", articleResult.getUrl());
            omsLogger.info(logThreadId() + "稿件错误数据存储成功: {}", articleResult.getUrl());
        } else {
            throw new RuntimeException("稿件错误数据存储失败: " + articleResult.getUrl());
        }
    }

    /**
     * 清洗稿件数据
     *
     * @param articleResult
     */
    public void articleDataFilter(ArticleResult articleResult) throws Exception {
        log.info("开始过滤数据:");
        try {
            Field[] fields = articleResult.getClass().getDeclaredFields();
            for (Field field : fields) {
                // 字段名
                String fieldName = field.getName();
                // 判断数据类型
                // String类型处理
                Class fieldType = field.getType();
                // log.info("{} 类型: {}", fieldName, fieldType);
                if (fieldType.equals(String.class)) {
                    Object fieldValueObj = field.get(articleResult);
                    if (oConvertUtils.isNotEmpty(fieldValueObj)) {
                        String fieldValue = fieldValueObj.toString();
                        // 过滤数据
                        if (fieldName.equals("content")) {
                            // 过滤文章详情
                            URL urlObj = new URL(articleResult.getUrl());
                            String baseUri = urlObj.getProtocol() + "://" + urlObj.getHost();
                            field.set(articleResult, PlaywrightDataFilter.filterArticleContent(fieldValue, baseUri));
                        } else {
                            field.set(articleResult, PlaywrightDataFilter.filterString(fieldValue));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("数据过滤错误");
            omsLogger.error(logThreadId() + "数据过滤错误");
            throw e;
        }
    }

    /**
     * 判断请求结果
     *
     * @param response
     * @return
     */
    public boolean checkResponse(Response response, String url) throws TimeoutError {
        // 请求出错
        if (oConvertUtils.isNotEmpty(response)) {
            if ( (response.status() <= 399) && (response.status() >= 200) ) {
                log.info("响应码: {}, {}", response.status(), url);
                omsLogger.info(logThreadId() + "响应码: {}, {}", response.status(), url);
                return true;
            } else {
                throw new TimeoutError(url + "请求失败,response.status: " + response.status());
            }
        } else {
            log.error("checkResponse Response 为空, response: {}", response);
            omsLogger.error("checkResponse Response 为空, response: {}", response);
            throw new TimeoutError(url + "Response 为空 null");
        }
    }

    /**
     * 获取代理IP
     *
     * @return
     */
    private String getProxyIP() {
        OkHttpClient okHttpClient = new OkHttpClient();
        log.info("proxyIPApi: {}", ipProxyApi);
        okhttp3.Request request = new okhttp3.Request.Builder().url(ipProxyApi).build();
        try {
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            String jsonResponse = response.body().string();
            log.info("获取代理IP: {}", jsonResponse);
            JSONObject objResponse = JSON.parseObject(jsonResponse);
            if (objResponse.getBoolean("success").equals(true)) {
                JSONObject resultObj = objResponse.getJSONObject("result");
                return resultObj.getString("scheme") + "://" + resultObj.getString("ip") + ":" + resultObj.getString("port");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            omsLogger.error(logThreadId() + e.getMessage());
        }
        log.error("获取代理IP失败");
        omsLogger.error(logThreadId() + "获取代理IP失败");
        return null;
    }

    /**
     * 获取userAgent
     *
     * @return
     */
    private String getUserAgent() {
        int random = (int)(Math.random() * 10 + 1);
        String ua = "";
        if ( (random % 2) == 1 ) {
            ua = FakeUa.generateMacChromeUa();
        } else {
            ua = FakeUa.generateWindowsChromeUa();
        }
        return ua;
    }

    @Override
    protected void finalize() throws Throwable {
        log.info("PlaywrightCrawl 执行 finalize");
        super.finalize();
        if (oConvertUtils.isNotEmpty(listPage)) {
            listPage.close();
        }
        if (oConvertUtils.isNotEmpty(articlePage)) {
            articlePage.close();
        }
        if (oConvertUtils.isNotEmpty(browserContext)) {
            browserContext.close();
        }
        if (oConvertUtils.isNotEmpty(browser)) {
            browser.close();
        }
        if (oConvertUtils.isNotEmpty(playwright)) {
            playwright.close();
        }
    }

    private String logThreadId() {
        return "[ThreadId: " + Thread.currentThread().getName() + "]  ";
    }

//    /**
//     * 【弃用】,被articlePageContentParser,listPageLocatorParser替代
//     * 列表页定位器,判断使用xpath还是regex进行定位,并处理相关逻辑
//     *
//     * @param match
//     * @param locator
//     * @return String
//     * @exception Exception
//     */
//    public String listPageLocator(String match, Locator locator) throws Exception {
//        String result = null;
//        if (oConvertUtils.isNotEmpty(match)) {
//            // 判断定位方式
//            if (match.startsWith(PlaywrightCrawl.XPATH_LOCATOR_PREFIX)) {
//                // xpath
//                log.info("xpath: {}", match);
//                // xpath
//                if (oConvertUtils.isNotEmpty(locator)) {
//                    result = locator.locator(match).textContent(textContentOptions);
//                } else {
//                    result = listPage.locator(match).textContent(textContentOptions);
//                }
//            } else if (match.startsWith(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX)) {
//                // 正则表达式,正则需要去除前缀标识才能运行
//                String newMatcher = match.replace(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX, "");
//                Matcher matcher = Pattern.compile(newMatcher).matcher(listPage.locator("//html").innerHTML(innerHTMLOptions));
//                if (matcher.find()) {
//                    log.info("regex : {}", result);
//                    result = matcher.group(1);
//                } else {
//                    log.info("regex not find");
//                }
//            } else {
//                // 其他不支持
//                throw new RuntimeException("不支持的定位器");
//            }
//        }
//        return result;
//    }

//    /**
//     * 【弃用】,被articlePageContentParser和articlePageLocatorParser替代
//     * 详情页页定位器,判断使用xpath还是regex进行定位,并处理相关逻辑
//     *
//     * @param match
//     * @return String
//     * @exception Exception
//     */
//    public String articlePageLocator(String match) throws Exception {
//        String result = null;
//        if (oConvertUtils.isNotEmpty(match)) {
//            // 判断定位方式
//            if (match.startsWith(PlaywrightCrawl.XPATH_LOCATOR_PREFIX)) {
//                // xpath
//                //log.info("xpath: {}", match);
//                result = articlePage.locator(match).textContent(textContentOptions);
//            } else if (match.startsWith(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX)) {
//                // 正则表达式,正则需要去除前缀标识才能运行
//                String newMatch = match.replace(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX, "");
//                Matcher matcher = Pattern.compile(newMatch).matcher(articlePage.locator("//html").innerHTML(innerHTMLOptions));
//                if (matcher.find()) {
//                    result = matcher.group(1);
//                    //log.info("regex : {}", result);
//                } else {
//                    // log.info("regex not find");
//                }
//            } else {
//                // 其他不支持
//                throw new RuntimeException("不支持的定位器: " + match);
//            }
//        }
//        return result;
//    }


    //    private void crawlLogger(String level, String var1, Object... var2) {
//        switch (level) {
//            case "info":
//                log.info(var1, var2);
//                omsLogger.info(var1, var2);
//                break;
//            case "error":
//                log.error(var1, var2);
//                omsLogger.error(var1, var2);
//                break;
//            case "debug":
//                log.debug(var1, var2);
//                omsLogger.debug(var1, var2);
//        }
//    }

    //    由于不是所有字段都需要规则采集,所以停用此遍历方法
//    public void articlePageLocator(ArticleResult articleResult, ArticleRuleNode articleRuleNode) throws Exception {
//        Field[] fields = articleResult.getClass().getDeclaredFields();
//        for (Field field : fields) {
//            String result = null;
//            String fieldName = field.getName();
//            String matchName = fieldName + "Match";
//            Field nodeFiled = articleRuleNode.getClass().getField(matchName);
//            Object matchObj = nodeFiled.get(articleRuleNode);
//            if (oConvertUtils.isNotEmpty(matchObj)) {
//                String match = matchObj.toString();
//                // 判断定位方式
//                if (match.startsWith(PlaywrightCrawl.XPATH_LOCATOR_PREFIX)) {
//                    // 判断字段
//                    if (fieldName.equals("content")) {
//                        // 稿件详情匹配,支持选取多个标签,然后合并
//                        List<Locator> contentLocatorList = articlePage.locator(match).all();
//                        List<String> contentList = contentLocatorList.stream().map(
//                                locator -> {
//                                    return locator.innerHTML();
//                                }
//                        ).collect(Collectors.toList());
//                        result = String.join("", contentList);
//                    } else {
//                        // 其他匹配
//                        result = articlePage.locator(match).textContent();
//                    }
//                    log.info("match is xpath: ");
//                    log.info("match is : {}", match);
//                    // xpath
//
//                    nodeFiled.set(articleRuleNode, result);
//                } else if (match.startsWith(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX)) {
//                    log.info("match is regex: ");
//                    // 正则表达式,正则需要去除前缀标识才能运行
//                    String newMatch = match.replace(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX, "");
//                    log.info("match is : {}", newMatch);
//                    Matcher matcher = Pattern.compile(newMatch).matcher(articlePage.content());
//                    log.info("groupcount: {}", matcher.groupCount());
//                    if (matcher.find()) {
//                        result = matcher.group(1);
//                        nodeFiled.set(articleRuleNode, result);
//                        log.info("regex is find: {}", result);
//                    } else {
//                        log.info("regex not find");
//                    }
//                } else {
//                    // 其他不支持
//                    throw new Exception(fieldName + ": 不支持的定位器");
//                }
//            }
//        }
//    }

}
