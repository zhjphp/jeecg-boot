package org.jeecg.modules.polymerize.playwright;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.URLUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.drawflow.*;
import org.jeecg.modules.polymerize.drawflow.model.*;
import org.jeecg.modules.polymerize.playwright.exception.RequestException;
import org.jeecg.modules.polymerize.playwright.filter.PlaywrightDataFilter;
import org.jeecg.modules.polymerize.playwright.parser.ApiParser;
import org.jeecg.modules.polymerize.playwright.parser.PageParser;
import org.jeecg.modules.polymerize.playwright.requester.ApiRequester;
import org.jeecg.modules.polymerize.playwright.ua.util.FakeUa;
import org.jeecg.modules.polymerize.playwright.util.OkHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @description: Playwright爬虫(测试规则用)
 * @author: wayne
 * @date 2023/6/8 17:52
 */
@Slf4j
//@RefreshScope
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CheckRulePlaywrightCrawl {

    private Playwright playwright;

    @Value("${polymerize.playwright.checkRuleEnableHeadless}")
    private boolean enableHeadless = false;

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

    @Resource
    private PageParser pageParser;

    @Resource
    private ApiRequester apiRequester;

    private ApiParser apiParser = SpringContextUtils.getApplicationContext().getBean(ApiParser.class);

    private OkHttpClient okHttpClient = OkHttpUtil.getOkHttpClient();

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
                        new BrowserType.LaunchOptions().setHeadless(enableHeadless).setDevtools(true)
                );
                break;
            case "firefox" :
                browser = playwright.firefox().launch(
                        new BrowserType.LaunchOptions().setHeadless(enableHeadless).setDevtools(true)
                );
                break;
            case "webkit" :
                browser = playwright.webkit().launch(
                        new BrowserType.LaunchOptions().setHeadless(enableHeadless).setDevtools(true)
                );
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * 配置browserContext,建立浏览器page
     *
     * @throws Exception
     */
    public void createPage(boolean restoreOriginalPage) throws Exception {
        // 已经打开的页面
        String currentListPageUrl = null;
        String currentArticlePageUrl = null;
        Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setJavaScriptEnabled(true);
        // 配置ua
        String userAgent = getUserAgent();
        log.info("使用userAgent: {}", userAgent);
        newContextOptions.setUserAgent(userAgent);
        // 配置代理ip
        if (enableIPProxy) {
            String proxyIP = getProxyIP();
            if (oConvertUtils.isNotEmpty(proxyIP)) {
                log.info("使用代理IP: {}", proxyIP);
                newContextOptions.setProxy(proxyIP);
            }
        }
        // 建立新页面前先关闭
        if (oConvertUtils.isNotEmpty(listPage)) {
            currentListPageUrl = listPage.url();
            listPage.close();
        }
        if (oConvertUtils.isNotEmpty(articlePage)) {
            currentArticlePageUrl = articlePage.url();
            articlePage.close();
        }
        if (oConvertUtils.isNotEmpty(browserContext)) {
            browserContext.close();
        }
        browserContext = browser.newContext(newContextOptions);
        // 屏蔽部分资源的加载
        // browserContext.route(disableLoadResource, route -> route.abort());
        browserContext.route(Pattern.compile(disableLoadResource), route -> route.abort());
        // 列表页
        listPage = browserContext.newPage();
        // 隐藏webdriver特征
        listPage.addInitScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");
        // 恢复原有页面
        if (restoreOriginalPage) {
            // 恢复原有页面
            if (oConvertUtils.isNotEmpty(currentListPageUrl)) {
                listPage.navigate(currentListPageUrl, navigateOptions);
                listPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                log.info("恢复原有listPage: {}", currentListPageUrl);
            }
        }
        // 详情页
        articlePage = browserContext.newPage();
        // 隐藏webdriver特征
        articlePage.addInitScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");
        // 恢复原有页面
        if (restoreOriginalPage) {
            // 恢复原有页面
            if (oConvertUtils.isNotEmpty(currentArticlePageUrl)) {
                articlePage.navigate(currentArticlePageUrl, navigateOptions);
                articlePage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                log.info("恢复原有articlePage: {}", currentArticlePageUrl);
            }
        }
    }

    /**
     * 滚动条到最下方
     * https://stackoverflow.com/questions/69183922/playwright-auto-scroll-to-bottom-of-infinite-scroll-page
     * https://blog.csdn.net/weixin_42152811/article/details/120828564
     * https://github.com/microsoft/playwright/issues/4302
     */
    private void scrollToBottom(Page page, int pageCount) {
        Object scrollHeight = page.evaluate(
                "() => document.documentElement.scrollHeight"
        );
        log.info("document.documentElement.scrollHeight: {}", scrollHeight.toString());
        double y = Double.parseDouble(scrollHeight.toString());
        for (int i = 0 ; i < pageCount; i ++) {
            page.mouse().wheel(0, y);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            // page.waitForTimeout(500);
        }
    }

    private void waterfallScrollToBottom(Page page, Integer pageCount, String bottomMatch, String pageMatch, String moreMatch) {
        Object scrollHeight = page.evaluate(
                "() => document.documentElement.scrollHeight"
        );
        log.info("document.documentElement.scrollHeight: {}", scrollHeight.toString());
        double y = Double.parseDouble(scrollHeight.toString());
        log.info("开始执行瀑布流下拉: {}", y);
        // 如果配置了底部特征
        if (oConvertUtils.isNotEmpty(bottomMatch)) {
            log.info("使用类底匹配规则");
            for (int i = 0 ; i < 1000000; i ++) {
                page.mouse().wheel(0, y);
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                try {
                    // 如果出现底部标识,则不在滚动
//                    if (oConvertUtils.isNotEmpty(page.locator(bottomMatch).innerHTML(innerHTMLOptions))) {
//                        break;
//                    }
                    if (page.locator(bottomMatch).isVisible()) {
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
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                log.info("屏数: {}", i);
                page.waitForTimeout(200);
            }
        } else {
            // 判断区块数量是否有变化
            log.info("使用自动判断是否到底");
            // 每次滚动之前的总条数
            int preCount = 0;
            // 捕获到的总条数,仅支持单指令,不需要pagePaser.parse解析
            int totalCount = listPage.locator(pageMatch).all().size();
            // 保险,防止无限循环
            int insure = 1000000;
            do {
                if (oConvertUtils.isNotEmpty(moreMatch)) {
                    try {
                        // Locator moreLocator = listPage.locator(moreMatch);
                        Locator moreLocator = listPageLocatorParser(moreMatch, null, page, null);
                        log.info("检查是否存在查看更多按钮: {}", moreLocator.count());
                        if ( moreLocator.count() == 1) {
                            log.info("点击查看更多按钮");
                            moreLocator.click();
                            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                            page.waitForTimeout(500);
                        }
                    } catch (TimeoutError e) {
                        // 捕获不存在元素的错误
                        log.info("找不到查看更多按钮");
                    }
                }
                for (int i = 0; i < 6; i++) {
                    log.info("向下滚动页面...");
                    page.mouse().wheel(0, y/2);
                    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    page.waitForTimeout(2000);
                }
                preCount = totalCount;
                totalCount = page.locator(pageMatch).all().size();
                log.info("totalCount: {}", totalCount);
                log.info("preCount: {}", preCount);
            } while ( (totalCount > preCount) && (insure > 0) );
            log.info("页面已经到底");
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

    public JSONObject testGetApiList(ApiListRuleNode apiListRuleNode) throws Exception {
        JSONObject show = new JSONObject();

        String startUrl = apiListRuleNode.getCheckRuleUrl();
        if (oConvertUtils.isEmpty(startUrl)) {
            throw new RuntimeException("测试页URL为空");
        }

        try {
            // 获取请求要素
            String method = apiListRuleNode.getMethod();
            String url = startUrl;
            String contentType = apiListRuleNode.getContentType();
            // 先解析自定义参数变量
            if (oConvertUtils.isNotEmpty(apiListRuleNode.getCustomParam())) {
                apiParser.parseCustomParam(apiListRuleNode.getCustomParam());
            }
            // 列表翻页循环关键参数
            // 总页数(默认1)
            Integer totalPage = 1;
            // 当前页码
            Integer currentPage = 1;
            // 是否翻页
            boolean isPageDown = true;
            // 预防置顶稿件
            int preventToppingCount = 0;
            // 展示页码
            String showPageKey = null;
            // 循环翻页
            while ( (currentPage <= totalPage) && isPageDown ) {
                // 先解析自定义参数变量 (因为自定义参数变量可能使用从结果中抽取的变量,所以每次都需要更新自定义变量)
                if (oConvertUtils.isNotEmpty(apiListRuleNode.getCustomParam())) {
                    log.info("解析自定义参数变量: {}", apiListRuleNode.getCustomParam());
                    apiParser.parseCustomParam(apiListRuleNode.getCustomParam());
                }
                // 解析URL中的占位符变量 (url中占位符为变量,所以每次需要重新解析)
                log.info("准备解析解析URL中的占位符变量: {}", url);
                url = apiParser.parseParamPlaceholder(url);
                log.info("目标URL: {}", url);
                // 请求相关参数
                String header = null;
                String body = null;
                String result = null;
                String urlParam = null;
                // 解析header (header中可能包含变量,所以每次需要重新解析)
                if (oConvertUtils.isNotEmpty(apiListRuleNode.getReqHeader())) {
                    header = apiParser.parseParamPlaceholder(apiListRuleNode.getReqHeader());
                    log.info("解析header: {}", header);
                }
                // 解析body (body中可能包含变量,所以每次需要重新解析)
                if (oConvertUtils.isNotEmpty(apiListRuleNode.getReqBody())) {
                    body = apiParser.parseParamPlaceholder(apiListRuleNode.getReqBody());
                    log.info("解析body: {}", body);
                }
                // 解析urlParam (url参数中可能包含变量,所以每次需要重新解析)
                if (oConvertUtils.isNotEmpty(apiListRuleNode.getReqUrlParam())) {
                    urlParam = apiParser.parseParamPlaceholder(apiListRuleNode.getReqUrlParam());
                    log.info("解析urlParam: {}", urlParam);
                }
                // 请求API
                result = apiRequester.request(method, url, contentType, header, body, urlParam);
                // 解析列表区块
                String listJSON = null;
                if (oConvertUtils.isNotEmpty(apiListRuleNode.getListMatch())) {
                    log.info("列表区块rule: {}", apiListRuleNode.getListMatch());
                    listJSON = apiParser.doParse(result, apiListRuleNode.getListMatch(), RuleNodeUtil.getFiledName(ApiListRuleNode::getListMatch), false);
                }
                log.info("解析列表区块 listJSON: {}", listJSON);
                JSONArray listJSONArray = JSONArray.parseArray(listJSON);
                // 总页数根据配置决定是否覆盖
                // 只在首次请求的时候进行覆盖
                if (currentPage == 1) {
                    // 判断是存在总页数配置
                    if (oConvertUtils.isNotEmpty(apiListRuleNode.getTotalPageMatch())) {
                        // 如果有总页数配置
                        String totalPageMatch = apiParser.doParse(result, apiListRuleNode.getTotalPageMatch(), RuleNodeUtil.getFiledName(ApiListRuleNode::getTotalPageMatch), true);
                        if (oConvertUtils.isNotEmpty(totalPageMatch)) {
                            totalPage = Integer.parseInt(totalPageMatch);
                            log.info("总页数匹配 totalPageMatch: {}", totalPageMatch);
                        }
                    } else if (oConvertUtils.isNotEmpty(apiListRuleNode.getTotalCountMatch())){
                        Integer pageSize = null;
                        // 如果有总数量配置
                        if (oConvertUtils.isNotEmpty(apiListRuleNode.getPageSizeMatch())) {
                            // 如果定义了页容量则使用容量配置
                            pageSize = Integer.parseInt(apiListRuleNode.getPageSizeMatch());
                            log.info("页容量使用pageSizeMatch: {}", pageSize);
                        } else {
                            // 如果没有定义页容量配置,则自动获取页容量
                            pageSize = listJSONArray.size();
                            log.info("页容量使用自动获取: {}", pageSize);
                        }
                        // 如果没有总页数,但是有总数量
                        String totalCountStr = apiParser.doParse(result, apiListRuleNode.getTotalCountMatch(), RuleNodeUtil.getFiledName(ApiListRuleNode::getTotalCountMatch), true);
                        Integer totalCount = Integer.parseInt(totalCountStr);
                        // 用总数量除页容量获取总页数
                        totalPage = totalCount/pageSize;
                        log.info("使用总稿件数量计算总页数: {}", totalPage);
                    }
                    log.info("总页数: {}", totalPage);
                }
                // 解析页面元素
                // 编列列表区块
                log.info("开始遍历列表区块");
                Iterator<Object> it = listJSONArray.iterator();
                // 记录列表区块解析结果
                List<ApiListResult> resultList = new ArrayList<>();
                while (it.hasNext()) {
                    ApiListResult apiListResult = new ApiListResult();
                    Object elementObj = it.next();
                    String elementJson = JSONObject.toJSONString(elementObj);
                    // 接口url
                    apiListResult.setApiUrl(url);
                    // 解析详情ID
                    String articleId = null;
                    if (oConvertUtils.isNotEmpty(apiListRuleNode.getArticleIdMatch())) {
                        articleId = apiParser.doParse(elementJson, apiListRuleNode.getArticleIdMatch(), RuleNodeUtil.getFiledName(ApiListRuleNode::getArticleIdMatch), false);
                    }
                    apiListResult.setArticleId(articleId);
                    log.info("解析详情ID: {}", articleId);
                    // 解析详情标题
                    String articleTitle = null;
                    if (oConvertUtils.isNotEmpty(apiListRuleNode.getArticleTitleMatch())) {
                        articleTitle = apiParser.doParse(elementJson, apiListRuleNode.getArticleTitleMatch(), RuleNodeUtil.getFiledName(ApiListRuleNode::getArticleTitleMatch), false);
                    }
                    apiListResult.setTitle(articleTitle);
                    log.info("解析详情标题: {}", articleTitle);
                    // 解析详情日期
                    String articleDate = null;
                    if (oConvertUtils.isNotEmpty(apiListRuleNode.getArticleDateMatch())) {
                        articleDate = apiParser.doParse(elementJson, apiListRuleNode.getArticleDateMatch(), RuleNodeUtil.getFiledName(ApiListRuleNode::getArticleDateMatch), false);
                    }
                    Date cutDate = null;
                    if (oConvertUtils.isNotEmpty(articleDate)) {
                        try {
                            log.info("格式化时间: {}", articleDate);
                            cutDate = DateUtils.cutDate(articleDate);
                            apiListResult.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cutDate));
                        } catch (RuntimeException e) {
                            log.warn("格式化时间错误: {}", articleDate);
                            apiListResult.setDate(null);
                        }
                    } else {
                        apiListResult.setDate(null);
                    }
                    apiListResult.setDate(articleDate);
                    log.info("解析详情日期: {}", articleDate);


                    // 判断日期是否为目标时间段的数据
                    if (
                            ( oConvertUtils.isNotEmpty(apiListRuleNode.getStartTime()) && oConvertUtils.isNotEmpty(cutDate) && cutDate.after(apiListRuleNode.getStartTime()) )
                    ) {
                        log.info("设定了有效时间段,在StartTime之后");
                        if (
                                ( oConvertUtils.isNotEmpty(apiListRuleNode.getEndTime()) && oConvertUtils.isNotEmpty(cutDate) && cutDate.before(apiListRuleNode.getEndTime()) )
                        ) {
                            // 如果设定了起止时间,且在时间段内,则为有效数据
                            log.info("在getEndTime()之前,为有效时间段数据");
                            // 记录采集结果
                            resultList.add(apiListResult);
                        } else {
                            log.info("不在getEndTime()之前,继续向前寻找");
                        }
                    } else if (
                            oConvertUtils.isEmpty(apiListRuleNode.getStartTime())
                                    &&
                                    oConvertUtils.isEmpty(apiListRuleNode.getEndTime())
                                    &&
                                    (apiListRuleNode.getEffectiveDays() > 0)
                                    &&
                                    oConvertUtils.isNotEmpty(cutDate)
                                    &&
                                    cutDate.after(
                                            org.apache.commons.lang.time.DateUtils.addDays(new Date(), apiListRuleNode.getEffectiveDays() * -1)
                                    )
                    ) {
                        // 如果没有设定起止时间,且设定了有效天数,且在有效天数内,则为有效数据
                        log.info("没有设定起止时间,且设定了有效天数,且在有效天数内,为有效数据");
                        // 记录采集结果
                        resultList.add(apiListResult);
                    } else if (
                            oConvertUtils.isEmpty(apiListRuleNode.getStartTime())
                                    &&
                                    oConvertUtils.isEmpty(apiListRuleNode.getEndTime())
                                    &&
                                    (apiListRuleNode.getEffectiveDays() == 0)
                    ) {
                        // 如果都没设定,则所有数据为有效
                        log.info("都没设定,所有数据为有效");
                        // 记录采集结果
                        resultList.add(apiListResult);
                    } else {
                        // 其他情况,丢弃数据
                        log.info("超出目标时间范围,目标起始时间: {}, 目标结束时间: {}, 目标有效天数: {}, 稿件时间: {}", apiListRuleNode.getStartTime(), apiListRuleNode.getStartTime(), apiListRuleNode.getEffectiveDays(), cutDate);
                        preventToppingCount++;
                        log.info("防止有置顶帖,继续处理,当前处理数量: {}, 配置容忍数量: {}", preventToppingCount, toppingCount);
                        if (preventToppingCount >= toppingCount) {
                            log.info("到达置顶帖容忍数量,停止翻页");
                            isPageDown = false;
                            break;
                        } else {
                            log.info("不停止翻页");
                        }
                    }

                }

                showPageKey = "page" + "-" + String.valueOf(currentPage);
                show.put(showPageKey, resultList);
                show.put(showPageKey + "-count", resultList.size());
                // 自定义结果参数抽取
                if (oConvertUtils.isNotEmpty(apiListRuleNode.getResultCustomParam())) {
                    JSONArray resultCustomParam = apiListRuleNode.getResultCustomParam();
                    apiParser.parseResultCustomParam(result, resultCustomParam);
                }
                log.info("all paramPool: {}", apiParser.paramPool.toString());

                // 采集完一个列表页,翻页
                currentPage++;
            }

        } catch (Exception e) {
            throw e;
        }

        return show;
    }

    /**
     * 可以使用${articleKey}变量值
     */
    public JSONObject testGetApiArticle(ApiArticleRuleNode apiArticleRuleNode, String articleId) throws Exception {
        log.info("apiArticleRuleNode: {}", apiArticleRuleNode.toString());
        apiParser = new ApiParser();
        JSONObject show = new JSONObject();

        // 获取请求要素
        String method = apiArticleRuleNode.getMethod();
        log.info("method: {}", method);
        String contentType = apiArticleRuleNode.getContentType();
        String articleUrl = apiArticleRuleNode.getCheckRuleUrl();

        ArticleResult articleResult = new ArticleResult();

        if (oConvertUtils.isEmpty(articleUrl)) {
            throw new Exception("测试页URL为空");
        }
        // 在变量池中加入或更新articleKey,可以在header或body中使用${articleKey}
        if (oConvertUtils.isNotEmpty(articleId)) {
            apiParser.addParamPool("articleId", articleId);
            log.info("指定系统预留变量: articleId={}", articleId);
        }
        // 因为自定义参数可以使用list页面中的变量,所以每次都需要更新自定义变量
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getArticleCustomParam())) {
            apiParser.parseCustomParam(apiArticleRuleNode.getArticleCustomParam());
        }
        articleUrl = apiParser.parseParamPlaceholder(articleUrl);
        // 因为header和body中可能使用从结果中抽取的变量,所以每次都需要重新组装header,body
        String header = null;
        String body = null;
        String urlParam = null;
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getReqHeader())) {
            header = apiParser.parseParamPlaceholder(apiArticleRuleNode.getReqHeader());
        }
        log.info("header: {}", header);
        // 解析body
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getReqBody())) {
            body = apiParser.parseParamPlaceholder(apiArticleRuleNode.getReqBody());
        }
        log.info("body: {}", body);
        // 解析urlParam
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getReqUrlParam())) {
            urlParam = apiParser.parseParamPlaceholder(apiArticleRuleNode.getReqUrlParam());
        }
        log.info("urlParam: {}", urlParam);
        // 请求API
        String result = apiRequester.request(method, articleUrl, contentType, header, body, urlParam);
        // 把响应数据写入变量池,可以当作预处理方法的参数
        apiParser.addParamPool("articleResponse", result);
        log.info(result);
        // 执行预处理指令,对结果进行预处理(例如解密文本等)
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getResultPreprocessor())) {
            result = apiParser.doParse(result, apiArticleRuleNode.getResultPreprocessor(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getResultPreprocessor), false);
            log.info("预处理请求结果: {}", result);
        }
        // 解析稿件内容
        // 解析详情标题
        log.info("标题匹配: {}", apiArticleRuleNode.getTitleMatch());
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getTitleMatch())) {
            String articleTitle = apiParser.doParse(result, apiArticleRuleNode.getTitleMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getTitleMatch), false);
            log.info("标题: {}, {}", apiArticleRuleNode.getTitleMatch(), articleTitle);
            articleResult.setTitle(articleTitle);
        }
        // 解析详情日期
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getDateMatch())) {
            String articleDate = apiParser.doParse(result, apiArticleRuleNode.getDateMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getDateMatch), false);
            articleResult.setDate(articleDate);
        }
        // 正文
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getContentMatch())) {
            String content = apiParser.doParse(result, apiArticleRuleNode.getContentMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getContentMatch), false);
            articleResult.setContent(content);
        }
        // 栏目
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getTopicMatch())) {
            String topic = apiParser.doParse(result, apiArticleRuleNode.getTopicMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getTopicMatch), false);
            articleResult.setTopic(topic);
        }
        // 子标题(元标签)
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getSubtitleMatch())) {
            String subtitle = apiParser.doParse(result, apiArticleRuleNode.getSubtitleMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getSubtitleMatch), false);
            articleResult.setSubtitle(subtitle);
        }
        // 来源
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getSourceMatch())) {
            String source = apiParser.doParse(result, apiArticleRuleNode.getSourceMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getSourceMatch), false);
            articleResult.setSource(source);
        }
        // 关键词
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getKeywordsMatch())) {
            String keywords = apiParser.doParse(result, apiArticleRuleNode.getKeywordsMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getKeywordsMatch), false);
            articleResult.setKeywords(keywords);
        }
        // 描述
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getDescriptionMatch())) {
            String description = apiParser.doParse(result, apiArticleRuleNode.getDescriptionMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getDescriptionMatch), false);
            articleResult.setDescription(description);
        }
        // 出处
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getReferenceMatch())) {
            String reference = apiParser.doParse(result, apiArticleRuleNode.getReferenceMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getReferenceMatch), false);
            articleResult.setReference(reference);
        }
        // 作者
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getAuthorMatch())) {
            String author = apiParser.doParse(result, apiArticleRuleNode.getAuthorMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getAuthorMatch), false);
            articleResult.setAuthor(author);
        }
        // 访问量
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getVisitMatch())) {
            String visit = apiParser.doParse(result, apiArticleRuleNode.getVisitMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getVisitMatch), false);
            articleResult.setVisit(visit);
        }
        // 评论量
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getCommentMatch())) {
            String comment = apiParser.doParse(result, apiArticleRuleNode.getCommentMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getCommentMatch), false);
            articleResult.setComment(comment);
        }
        // 收藏量
        if (oConvertUtils.isNotEmpty(apiArticleRuleNode.getCollectMatch())) {
            String collect = apiParser.doParse(result, apiArticleRuleNode.getCollectMatch(), RuleNodeUtil.getFiledName(ApiArticleRuleNode::getCollectMatch), false);
            articleResult.setCollect(collect);
        }
        log.info("articleResult: {}", articleResult);
        show = (JSONObject) JSON.toJSON(articleResult);

        return show;
    }

    /**
     * 采集列表(测试规则用)
     *
     * @param listRuleNode
     * @throws Exception
     */
    public JSONObject testGetList(ListRuleNode listRuleNode) throws Exception {
        // 初始化定位器超时时间
        textContentOptions = new Locator.TextContentOptions().setTimeout(locatorTimeout);
        innerHTMLOptions = new Locator.InnerHTMLOptions().setTimeout(locatorTimeout);
        getAttributeOptions = new Locator.GetAttributeOptions().setTimeout(locatorTimeout);
        navigateOptions = new Page.NavigateOptions().setTimeout(pageNavigateTimeout);
        waitForSelectorOptions = new Page.WaitForSelectorOptions().setTimeout(waitForSelectorOptionsTimeout);
        // 配置locatorParser
        pageParser.setTextContentOptions(textContentOptions);
        pageParser.setGetAttributeOptions(getAttributeOptions);
        pageParser.setInnerHTMLOptions(innerHTMLOptions);

        JSONObject show = new JSONObject();
        // 测试规则专用url
        String startUrl = listRuleNode.getCheckRuleUrl();
        if (oConvertUtils.isEmpty(startUrl)) {
            throw new RuntimeException("测试页URL为空");
        }
        try {
            // 全局配置
            initPlaywright();
            createBrowser();
            // 设置指定资源加载配置,如果配置了要屏蔽的资源,则覆盖默认的ncaos中的骗配置
            log.info("自定义配置: {}", listRuleNode.getCheckRuleDisableLoadResource());
            if (oConvertUtils.isNotEmpty(listRuleNode.getCheckRuleDisableLoadResource())) {
                log.info("使用自定义配置: {}", listRuleNode.getCheckRuleDisableLoadResource());
                this.disableLoadResource = listRuleNode.getCheckRuleDisableLoadResource();
            }
            log.info("资源屏蔽配置：{}", this.disableLoadResource);
            createPage(false);

            Response response = listPage.navigate(startUrl, navigateOptions);
            if (!checkResponse(response)) {
                throw new RequestException(startUrl + "请求失败,response.status: " + response.status());
            }

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
                    try {
                        totalPage = Integer.parseInt(tmpTotalPage);
                    } catch (RuntimeException e) {
                        throw new RuntimeException("总页数匹配错误: " +  e.getMessage());
                    }
                } catch (TimeoutError e) {
                    log.warn("没有匹配到总页数,默认只有一页");
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
            show.put("totalPage", totalPage);
            // 是否翻页
            boolean isPageDown = true;
            // 当前页码
            int currentPage = 0;
            // 预防置顶稿件
            int preventToppingCount = 0;
            // 判断稿件日期是否符合目标区间,是否翻页继续爬取
            // 测试方法最多翻一页
            while ( (currentPage < totalPage) && isPageDown ) {
                String showPageKey = "page" + "-" + String.valueOf(currentPage);
                // 总发现稿件数量
                int totalCount = 0;
                log.info("当前页序号: {}", currentPage);
                // 一页列表结果
                List<ListResult> resultList = new ArrayList<>();
                // 一页列表结果
                List<ListResult> resultErrorList = new ArrayList<>();
                // 等待加载完成
                listPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                // 如果只用waitForLoadState对于页面渲染较慢的网站无法抓取到内容,采取等待标志性元素探测的方式判断页面是否加载完成,此处使用稿件正文内容进行探测
                try {
                    pageParser.waitForSelector(listRuleNode.getPageMatch(), listPage);
                } catch (TimeoutError e) {
                    // 此处不做处理,页面继续执行
                    log.warn("加载列表页未找到区块定位内容");
                }
                // listPage.waitForTimeout(5000);
                // 滚动条到底
                int scrollCount = listPageScrollPageCount;
                if (listRuleNode.getWaterfallFlag()) {
                    log.info("当前页面为瀑布流页面");
                    // 瀑布流
                    waterfallScrollToBottom(
                            listPage, listRuleNode.getWaterfallPageCount(), listRuleNode.getWaterfallBottomMatch(), listRuleNode.getPageMatch(), listRuleNode.getMoreMatch()
                    );
                } else {
                    log.info("当前页面为常规页面");
                    scrollToBottom(listPage, scrollCount);
                }
                // 匹配区块,仅支持单指令,不需要pagePaser.parse解析
                List<Locator> locators = listPage.locator(listRuleNode.getPageMatch()).all();
                log.info("locators 数量: {}", locators.size());
                // 遍历每个区块
                for (Locator locator: locators) {
                    try {
                        totalCount++;
                        ListResult listResult = new ListResult();
                        // 获取标题
                        String title = null;
                        if (oConvertUtils.isNotEmpty(listRuleNode.getArticleTitleMatch())) {
                            title = listPageContentParser(listRuleNode.getArticleTitleMatch(), locator, null, RuleNodeUtil.getFiledName(ListRuleNode::getArticleTitleMatch));
                        }
                        listResult.setTitle(title);
                        // 获取日期
                        String dateStr = null;
                        if (oConvertUtils.isNotEmpty(listRuleNode.getArticleDateMatch())) {
                            dateStr = listPageContentParser(listRuleNode.getArticleDateMatch(), locator, null, RuleNodeUtil.getFiledName(ListRuleNode::getArticleDateMatch));
                        }
                        Date cutDate = null;
                        if (oConvertUtils.isNotEmpty(dateStr)) {
                            try {
                                cutDate = DateUtils.cutDate(dateStr);
                                listResult.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cutDate));
                            } catch (RuntimeException e) {
                                log.info("捕获时间无法格式化错误");
                                listResult.setDate(null);
                            }
                        } else {
                            listResult.setDate(null);
                        }
                        // 获取链接
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
                        listResult.setUrl(articleUrl);
                        log.info("列表条目: {}", listResult.toString());
                        // 判断取出的url是否是外链
                        if ( oConvertUtils.isEmpty(articleUrl)) {
                            // 如果没有取到文章链接则不处理
                            log.info("无法获取稿件url");
                            resultErrorList.add(listResult);
                        } else if (!URLUtils.isSameDomainName(articleUrl, listPage.url()) && !listRuleNode.getEnableOutside()) {
                            log.info("当前条目为外链内容,不进行采集: {}", listResult.toString());
                            resultErrorList.add(listResult);
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
                                    log.info("不在getEndTime()之前,继续翻页寻找");
                                    resultErrorList.add(listResult);
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
                                resultErrorList.add(listResult);
                                // 其他情况,丢弃数据
                                log.info("超出目标时间范围,目标起始时间: {}, 目标结束时间: {}, 目标有效天数: {}, 稿件时间: {}", listRuleNode.getStartTime(), listRuleNode.getEndTime(), listRuleNode.getEffectiveDays(), cutDate);
                                if (preventToppingCount >= toppingCount) {
                                    isPageDown = false;
                                    break;
                                }
                                log.info("防止有置顶帖,继续处理,当前处理数量: {}, 配置容忍数量: {}", preventToppingCount, toppingCount);
                                preventToppingCount++;
                            }
                        }
                    } catch (TimeoutError e) {
                        // 如果列表中有元素缺失属性,有可能是一些无关元素,所以忽略错误,继续执行
                        log.error(listPage.url() + e.getMessage());
                    }
                }
                show.put(showPageKey + "-Error", resultErrorList);
                show.put(showPageKey, resultList);
                show.put(showPageKey + "-" + "count", resultList.size());
                show.put(showPageKey + "-" + "count-find", totalCount);

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
                            log.info("下一页按钮禁用,列表执行结束,停止翻");
                            break;
                        } else {
                            log.info("点击下一页");
                            nextButton.click();
                            currentPage++;
                            listPage.waitForTimeout(sleepTime);
                        }
                    } else {
                        log.info("无法定位下一页按钮,列表执行结束,停止翻页");
                        break;
                    }
                    // Thread.sleep(sleepTime);
                } else {
                    log.info("列表执行结束,停止翻页");
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (oConvertUtils.isNotEmpty(listPage)) {
                log.info("关闭 listPage");
                listPage.close();
            }
            if (oConvertUtils.isNotEmpty(articlePage)) {
                log.info("关闭 articlePage");
                articlePage.close();
            }
            if (oConvertUtils.isNotEmpty(browserContext)) {
                log.info("关闭 browserContext");
                browserContext.close();
            }
        }
        log.info("结束");
        return show;
    }

    /**
     * 采集详情(测试规则用)
     *
     * @param articleRuleNode
     * @throws Exception
     */
    public JSONObject testGetArticle(ArticleRuleNode articleRuleNode) throws Exception {
        // 初始化定位器超时时间
        textContentOptions = new Locator.TextContentOptions().setTimeout(locatorTimeout);
        innerHTMLOptions = new Locator.InnerHTMLOptions().setTimeout(locatorTimeout);
        getAttributeOptions = new Locator.GetAttributeOptions().setTimeout(locatorTimeout);
        navigateOptions = new Page.NavigateOptions().setTimeout(pageNavigateTimeout);

        pageParser.setTextContentOptions(textContentOptions);
        pageParser.setGetAttributeOptions(getAttributeOptions);
        pageParser.setInnerHTMLOptions(innerHTMLOptions);

        JSONObject show = new JSONObject();
        String articleUrl = articleRuleNode.getCheckRuleUrl();
        if (oConvertUtils.isEmpty(articleUrl)) {
            throw new Exception("测试页URL为空");
        }

        // 判断当前节点规则是否可用
        if (oConvertUtils.isEmpty(articleRuleNode.getRuleSelectByDomainMatch())) {
            // 1. 如果没有配置适用域名为有效
            log.info("有效规则,规则没有适用域名");
        } else {
            // 2. 如果配置了适用域名,则判断是否适用于当前域名
            // 获取当前域名
            String tmpDomain = new URL(articleUrl).getHost();
            if (tmpDomain.matches(articleRuleNode.getRuleSelectByDomainMatch())) {
                // 2.1 适用于当前域名则 push 进入LinkedList,队头存放最适用的规则,通过 pop 优先使用此规则
                log.info("有效规则,规则适配域名");
            } else {
                // 2.1 不适用于当前域名则不使用此规则
                log.info("无效规则,不适配当前域名");
                throw new RuntimeException("域名适配规则: " + articleRuleNode.getRuleSelectByDomainMatch() + ", 不适配当前域名: " + articleUrl);
            }
        }

        ArticleResult articleResult = new ArticleResult();
        try {
            // 全局配置
            initPlaywright();
            createBrowser();
            // 设置指定资源加载配置,如果配置了要屏蔽的资源,则覆盖默认的ncaos中的骗配置
            if (oConvertUtils.isNotEmpty(articleRuleNode.getCheckRuleDisableLoadResource())) {
                this.disableLoadResource = articleRuleNode.getCheckRuleDisableLoadResource();
            }
            log.info("资源屏蔽配置：{}", this.disableLoadResource);
            createPage(false);

            // 超时重试一次
            int tries = 0;
            int tryCount = 2;
            while (tries < 2) {
                try {
                    log.info("开始采集稿件: {}, 节点规则: {}", articleUrl, articleRuleNode.toString());
                    // 打开页面
                    Response response = articlePage.navigate(articleUrl, navigateOptions);
                    if (!checkResponse(response)) {
                        throw new TimeoutError(articleUrl + "请求失败,response.status: " + response.status());
                    }
                    // 等待加载完成
                    articlePage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    // 如果只用waitForLoadState对于页面渲染较慢的网站无法抓取到内容,采取等待标志性元素探测的方式判断页面是否加载完成,此处使用稿件正文内容进行探测
                    try {
                        pageParser.waitForSelector(articleRuleNode.getContentMatch(), articlePage);
                    } catch (TimeoutError e) {
                        // 此处不做处理,页面继续执行
                        log.warn("加载详情页面未找到详情定位内容");
                    }
                    // 滚动条到底
                    scrollToBottom(articlePage, articlePageScrollPageCount);
                    // 判断是否需要点击后查看更多
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getMoreButtonMatch())) {
                        Locator moreButton = articlePageLocatorParser(articleRuleNode.getMoreButtonMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getMoreButtonMatch));
                        moreButton.click();
                        // 等待加载完成
                        articlePage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                        // 滚动条到底
                        scrollToBottom(articlePage, articlePageScrollPageCount);
                    }
                    log.info("网页源代码: {}", articlePage.locator("//html").innerHTML());
                    // 匹配内容
                    // 判断是否为单页采集,如果为单页采集,则忽略传入的URL,使用配置中的url
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getSingleFlag()) && articleRuleNode.getSingleFlag()) {
                        articleUrl = articleRuleNode.getSingleUrl();
                    }
                    // 按配固定则匹配内容
                    articleResult.setUrl(articleUrl);
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
                    // 关键词
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getKeywordsMatch())) {
                        String keywords = articlePageContentParser(articleRuleNode.getKeywordsMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getTopicMatch));
                        articleResult.setKeywords(keywords);
                    }
                    // 描述
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getDescriptionMatch())) {
                        String description = articlePageContentParser(articleRuleNode.getDescriptionMatch(), null, articlePage, RuleNodeUtil.getFiledName(ArticleRuleNode::getDescriptionMatch));
                        articleResult.setDescription(description);
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
                    // log.info("稿件内容采集完成: \nurl: {} \ntitle: {} \narticleResult: {}", articleResult.getUrl(), articleResult.getTitle(), articleResult.toString());
                    log.info("稿件内容采集完成: \nurl: {} \ntitle: {} ", articleResult.getUrl(), articleResult.getTitle());
                    // 自定义配置暂时用不到 articleRuleNode.getCustomConfig();
                    articlePage.waitForTimeout(sleepTime);
                    break;
                } catch (PlaywrightException e) {
                    if (e.getClass().getName().equals("com.microsoft.playwright.TimeoutError") || e.getMessage().contains("net::ERR_NAME_NOT_RESOLVED")) {
                        tries++;
                        if (tries == tryCount) {
                            log.error("{},尝试请求 {} 次失败", articleUrl, tries);
                            throw e;
                        }
                        log.info("更换代理IP和UA,尝试重新请求: {}", articleUrl);
                        // 更换代理IP和UA
                        createPage(true);
                    } else {
                        throw e;
                    }
                }
            }
            articleDataFilter(articleResult);
            show = (JSONObject) JSON.toJSON(articleResult);
        } catch (Exception e) {
            throw e;
        } finally {
            listPage.close();
            articlePage.close();
            browserContext.close();
        }

        return show;
    }

    /**
     * 清洗稿件数据
     *
     * @param articleResult
     */
    public void articleDataFilter(ArticleResult articleResult) throws Exception {
        try {
            Field[] fields = articleResult.getClass().getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                log.info("fieldName: {}", fieldName);
                Object fieldValueObj = field.get(articleResult);
                if (oConvertUtils.isNotEmpty(fieldValueObj)) {
                    String fieldValue = fieldValueObj.toString();
                    // 过滤数据
                    if (fieldName.equals("content")) {
                        log.info("执行filterArticleContent");
                        log.info("content: {}", fieldValue);
                        // 过滤文章详情
                        URL urlObj = new URL(articleResult.getUrl());
                        String baseUri = urlObj.getProtocol() + "://" + urlObj.getHost();
                        field.set(articleResult, PlaywrightDataFilter.filterArticleContent(fieldValue, baseUri));
                    } else {
                        log.info("执行filterString");
                        // 过滤除详情外其他字段
                        field.set(articleResult, PlaywrightDataFilter.filterString(fieldValue));
                    }
                }
            }
        } catch (Exception e) {
            log.error("数据清洗错误");
            throw e;
        }
    }

    /**
     * 判断请求结果
     *
     * @param response
     * @return
     */
    public boolean checkResponse(Response response) {
        // 请求出错
        return (response.status() <= 399) && (response.status() >= 200);
    }

    /**
     * 获取代理IP
     *
     * @return
     */
    private String getProxyIP() {
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
        }
        log.error("获取代理IP失败");
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
            ua = FakeUa.generateMacFirefoxUa();
        } else {
            ua = FakeUa.generateWindowsFirefoxUa();
        }
        return ua;
    }

    //    已经被 listPageContentParser, listPageLocatorParser 替代
//    public String listPageLocator(String match, Locator locator) throws Exception {
//        String result = null;
//        if (oConvertUtils.isNotEmpty(match)) {
//            // 判断定位方式
//            if (match.startsWith(PlaywrightCrawl.XPATH_LOCATOR_PREFIX)) {
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
//                    result = matcher.group(1);
//                }
//            } else {
//                // 其他不支持
//                throw new Exception("不支持的定位器: " + match);
//            }
//        }
//        return result;
//    }

//    /**
//     * 【弃用】已经被 articlePageContentParser 替代，
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
//                log.info("xpath: {}", match);
//                result = articlePage.locator(match).textContent(textContentOptions);
//            } else if (match.startsWith(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX)) {
//                // 正则表达式,正则需要去除前缀标识才能运行
//                String newMatch = match.replace(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX, "");
//                Matcher matcher = Pattern.compile(newMatch).matcher(articlePage.locator("//html").innerHTML(innerHTMLOptions));
//                if (matcher.find()) {
//                    result = matcher.group(1);
//                    log.info("regex: {}", result);
//                } else {
//                    log.info("regex not find");
//                }
//            } else {
//                // 其他不支持
//                throw new Exception("不支持的定位器: " + match);
//            }
//        }
//        return result;
//    }

    //    【弃用】由于不是所有字段都需要规则采集,所以停用此遍历方法
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
