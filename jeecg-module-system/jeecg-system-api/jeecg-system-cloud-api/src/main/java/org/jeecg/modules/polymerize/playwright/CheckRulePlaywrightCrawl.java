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
import org.jeecg.modules.polymerize.playwright.exception.RequestException;
import org.jeecg.modules.polymerize.playwright.filter.PlaywrightDataFilter;
import org.jeecg.modules.polymerize.playwright.ua.util.FakeUa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
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
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CheckRulePlaywrightCrawl {

    private Playwright playwright;

    // @Value("${polymerize.playwright.enableHeadless}")
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

    public static final String XPATH_LOCATOR_PREFIX = "xpath=";

    public static final String REGULAR_EXPRESSION_LOCATOR_PREFIX = "regex=";

    /**Locator超时时间配置*/
    @Value("${polymerize.playwright.locatorTimeout}")
    private double locatorTimeout;

    /**page.navigat超时时间配置*/
    @Value("${polymerize.playwright.pageNavigateTimeout}")
    private double pageNavigateTimeout;

    /**Locator超时时间配置*/
    private Locator.TextContentOptions textContentOptions;

    /**Locator超时时间配置*/
    private Locator.InnerHTMLOptions innerHTMLOptions;

    /**Locator超时时间配置*/
    private Locator.GetAttributeOptions getAttributeOptions;

    /**page.navigate超时时间配置*/
    private Page.NavigateOptions navigateOptions;

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
    public void createPage() throws Exception {
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
        if (oConvertUtils.isNotEmpty(currentListPageUrl)) {
            listPage.navigate(currentListPageUrl, navigateOptions);
        }
        // 详情页
        articlePage = browserContext.newPage();
        // 隐藏webdriver特征
        articlePage.addInitScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");
        // 恢复原有页面
        if (oConvertUtils.isNotEmpty(currentArticlePageUrl)) {
            articlePage.navigate(currentArticlePageUrl, navigateOptions);
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
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                log.info("屏数: {}", i);
                page.waitForTimeout(200);
            }
        } else {
            // 判断区块数量是否有变化
            log.info("使用自动判断是否到底");
            int preCount = 0;
            int totalCount = 0;
            int tmpCount = 0;
            // 保险,防止无线循环
            int insure = 1000000;
            do {
                if (oConvertUtils.isNotEmpty(moreMatch)) {
                    try {
                        Locator moreLocator = listPage.locator(moreMatch);
                        log.info("检查是否存在查看更多按钮: {}", moreLocator.count());
                        if ( moreLocator.count() == 1) {
                            log.info("点击查看更多按钮");
                            moreLocator.click();
                            listPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                            listPage.waitForTimeout(500);
                        }
                    } catch (TimeoutError e) {
                        // 捕获不存在元素的错误
                        log.info("找不到查看更多按钮");
                    }
                }
                for (int i = 0; i < 3; i++) {
                    page.mouse().wheel(0, y);
                    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    page.waitForTimeout(2000);
                }
                preCount = totalCount;
                totalCount = page.locator(pageMatch).all().size();
                log.info("totalCount: {}", totalCount);
                log.info("preCount: {}", preCount);
            } while ( (totalCount > preCount) && (insure > 0) );
        }
    }

    public String listPageLocator(String match, Locator locator) throws Exception {
        String result = null;
        if (oConvertUtils.isNotEmpty(match)) {
            // 判断定位方式
            if (match.startsWith(PlaywrightCrawl.XPATH_LOCATOR_PREFIX)) {
                // xpath
                if (oConvertUtils.isNotEmpty(locator)) {
                    result = locator.locator(match).textContent(textContentOptions);
                } else {
                    result = listPage.locator(match).textContent(textContentOptions);
                }
            } else if (match.startsWith(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX)) {
                // 正则表达式,正则需要去除前缀标识才能运行
                String newMatcher = match.replace(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX, "");
                Matcher matcher = Pattern.compile(newMatcher).matcher(listPage.locator("//html").innerHTML(innerHTMLOptions));
                if (matcher.find()) {
                    result = matcher.group(1);
                }
            } else {
                // 其他不支持
                throw new Exception("不支持的定位器: " + match);
            }
        }
        return result;
    }

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

    /**
     * 详情页页定位器,判断使用xpath还是regex进行定位,并处理相关逻辑
     *
     * @param match
     * @return String
     * @exception Exception
     */
    public String articlePageLocator(String match) throws Exception {
        String result = null;
        if (oConvertUtils.isNotEmpty(match)) {
            // 判断定位方式
            if (match.startsWith(PlaywrightCrawl.XPATH_LOCATOR_PREFIX)) {
                // xpath
                log.info("xpath: {}", match);
                result = articlePage.locator(match).textContent(textContentOptions);
            } else if (match.startsWith(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX)) {
                // 正则表达式,正则需要去除前缀标识才能运行
                String newMatch = match.replace(PlaywrightCrawl.REGULAR_EXPRESSION_LOCATOR_PREFIX, "");
                Matcher matcher = Pattern.compile(newMatch).matcher(articlePage.locator("//html").innerHTML(innerHTMLOptions));
                if (matcher.find()) {
                    result = matcher.group(1);
                    log.info("regex: {}", result);
                } else {
                    log.info("regex not find");
                }
            } else {
                // 其他不支持
                throw new Exception("不支持的定位器: " + match);
            }
        }
        return result;
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
            createPage();

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
                    String tmpTotalPage = listPageLocator(listRuleNode.getTotalPageMatch(), null);
                    totalPage = Integer.parseInt(tmpTotalPage);
                } catch (TimeoutError e) {
                    log.warn("没有匹配到总页数,默认只有一页");
                }
                log.info("指定总页数匹配: {}", totalPage);
            } else if (oConvertUtils.isNotEmpty(listRuleNode.getTotalCountMatch())) {
                log.info("使用总稿件数量匹配");
                // 判断是否配置总稿件数量匹配
                Integer totalCount = Integer.parseInt(listPageLocator(listRuleNode.getTotalCountMatch(), null));
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
                // 匹配区块
                List<Locator> locators = listPage.locator(listRuleNode.getPageMatch()).all();
                log.info("locators 数量: {}", locators.size());
                // 遍历每个区块
                for (Locator locator: locators) {
                    totalCount++;
                    ListResult listResult = new ListResult();
                    String title = listPageLocator(listRuleNode.getArticleTitleMatch(), locator);
                    listResult.setTitle(title);
                    String dateStr = listPageLocator(listRuleNode.getArticleDateMatch(), locator);
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
                    // 有部分情况<a>标签就是列表最外层元素,所以需要判断最外层元素是否有href属性,如果没有才去内存取
                    String articleUrl = null;
                    if (oConvertUtils.isNotEmpty(locator.getAttribute("href", getAttributeOptions))) {
                        // 如果最外层有链接则取外层
                        articleUrl = locator.getAttribute("href", getAttributeOptions);
                        log.info("取定位外层href属性, articleUrl: {}", articleUrl);
                    } else {
                        // 否则则使用内层定位链接
                        if (oConvertUtils.isNotEmpty(listRuleNode.getArticleUrlMatch())) {
                            // 详情URL仅支持xpath定位方式
                            if (!listRuleNode.getArticleUrlMatch().startsWith(PlaywrightCrawl.XPATH_LOCATOR_PREFIX)) {
                                throw new Exception("稿件URL仅支持xpath定位");
                            }
                            articleUrl = locator.locator(listRuleNode.getArticleUrlMatch()).getAttribute("href", getAttributeOptions);
                            log.info("取内层定位配置, articleUrl: {}", articleUrl);
                        }
                    }
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
                    Locator nextButton = listPage.locator(listRuleNode.getNextMatch());
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
            listPage.close();
            articlePage.close();
            browserContext.close();
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
        JSONObject show = new JSONObject();
        String articleUrl = articleRuleNode.getCheckRuleUrl();
        if (oConvertUtils.isEmpty(articleUrl)) {
            throw new Exception("测试页URL为空");
        }
        ArticleResult articleResult = new ArticleResult();
        try {
            // 全局配置
            initPlaywright();
            createBrowser();
            createPage();

            // 超时重试一次
            int tries = 0;
            int tryCount = 2;
            while (tries < 2) {
                try {
                    log.info("开始采集稿件: {}, 节点规则: {}", articleUrl, articleRuleNode.toString());
                    // 打开页面
                    Response response = articlePage.navigate(articleUrl, navigateOptions);
                    // 等待加载完成
                    articlePage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    // 滚动条到底
                    scrollToBottom(articlePage, articlePageScrollPageCount);
                    // 判断是否需要点击后查看更多
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getMoreButtonMatch())) {
                        if (!articleRuleNode.getMoreButtonMatch().startsWith(PlaywrightCrawl.XPATH_LOCATOR_PREFIX)) {
                            throw new Exception("查看更多仅支持xpath匹配方式");
                        }
                        articlePage.locator(articleRuleNode.getMoreButtonMatch()).click();
                        // 等待加载完成
                        articlePage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                        // 滚动条到底
                        scrollToBottom(articlePage, articlePageScrollPageCount);
                    }
                    if (!checkResponse(response)) {
                        throw new TimeoutError(articleUrl + "请求失败,response.status: " + response.status());
                    }
                    log.info("网页源代码: {}", articlePage.locator("//html").innerHTML());
                    // 匹配内容
                    // 判断是否为单页采集,如果为单页采集,则忽略传入的URL,使用配置中的url
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getSingleFlag()) && articleRuleNode.getSingleFlag()) {
                        articleUrl = articleRuleNode.getSingleUrl();
                    }
                    // 按配固定则匹配内容
                    articleResult.setUrl(articleUrl);
                    if (articlePage.locator("//meta[@name='keywords']").count() == 1) {
                        articleResult.setKeywords(articlePage.locator("//meta[@name='keywords']").getAttribute("content", getAttributeOptions));
                    }
                    if (articlePage.locator("//meta[@name='description']").count() == 1) {
                        articleResult.setDescription(articlePage.locator("//meta[@name='description']").getAttribute("content", getAttributeOptions));
                    }
                    // 按配置规则匹配内容
                    // articleResult.setContent(articlePageLocator(articleRuleNode.getContentMatch()));
                    // 稿件详情暂时单独处理,支持多个详情区块
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getContentMatch())) {
                        if (!articleRuleNode.getContentMatch().startsWith(PlaywrightCrawl.XPATH_LOCATOR_PREFIX)) {
                            throw new Exception("稿件详情只支持xpath提取规则");
                        }
                        List<Locator> contentLocatorList = articlePage.locator(articleRuleNode.getContentMatch()).all();
                        List<String> contentList = contentLocatorList.stream().map(
                                locator -> {
                                    return locator.innerHTML(innerHTMLOptions);
                                }
                        ).collect(Collectors.toList());
                        articleResult.setContent(String.join("", contentList));
                    }
                    articleResult.setTopic(articlePageLocator(articleRuleNode.getTopicMatch()));
                    articleResult.setTitle(articlePageLocator(articleRuleNode.getTitleMatch()));
                    articleResult.setSubtitle(articlePageLocator(articleRuleNode.getSubtitleMatch()));
                    articleResult.setDate(articlePageLocator(articleRuleNode.getDateMatch()));
                    articleResult.setReference(articlePageLocator(articleRuleNode.getReferenceMatch()));
                    articleResult.setSource(articlePageLocator(articleRuleNode.getSourceMatch()));
                    articleResult.setAuthor(articlePageLocator(articleRuleNode.getAuthorMatch()));
                    articleResult.setVisit(articlePageLocator(articleRuleNode.getVisitMatch()));
                    articleResult.setComment(articlePageLocator(articleRuleNode.getCommentMatch()));
                    articleResult.setCollect(articlePageLocator(articleRuleNode.getCollectMatch()));

                    // log.info("稿件内容采集完成: \nurl: {} \ntitle: {} \narticleResult: {}", articleResult.getUrl(), articleResult.getTitle(), articleResult.toString());
                    log.info("稿件内容采集完成: \nurl: {} \ntitle: {} ", articleResult.getUrl(), articleResult.getTitle());
                    // 不需要处理 articleRuleNode.getCustomConfig();
                    // Thread.sleep(sleepTime);
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
                        createPage();
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


}
