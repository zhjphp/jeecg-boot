package org.jeecg.modules.polymerize.playwright;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.drawflow.*;
import org.jeecg.modules.polymerize.drawflow.model.*;
import org.jeecg.modules.polymerize.playwright.exception.RequestException;
import org.jeecg.modules.polymerize.playwright.ua.util.FakeUa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
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


    @Autowired
    private Playwright playwright;

    @Autowired
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

    /**请求超时时间(毫秒)*/
    @Value("${polymerize.playwright.timeout}")
    private double timeout;

    /**预计置顶贴数量*/
    @Value("${polymerize.playwright.toppingCount}")
    private int toppingCount;

    private BrowserContext browserContext;

    /**列表页面*/
    private Page listPage;

    /**详情页面*/
    private Page articlePage;

    private Drawflow drawflow;

    /**
     * 配置browserContext,建立浏览器page
     *
     * @throws Exception
     */
    public void createPage() throws Exception {
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
            log.info("关闭列表页");
            listPage.close();
        }
        if (oConvertUtils.isNotEmpty(articlePage)) {
            log.info("关闭详情页");
            articlePage.close();
        }
        if (oConvertUtils.isNotEmpty(browserContext)) {
            log.info("关闭browserContext");
            browserContext.close();
        }
        browserContext = browser.newContext(newContextOptions);
        log.info("新建browserContext");
        // 屏蔽部分资源的加载
        browserContext.route("**/*.{png,PNG,jpg,JPG,jpeg,JPEG,gif,GIF,mp4,MP4,mp3,MP3,pdf,PDF,css,CSS}", route -> route.abort());
        // 列表页
        listPage = browserContext.newPage();
        log.info("新建listPage");
        // 隐藏webdriver特征
        listPage.addInitScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");
        // 详情页
        articlePage = browserContext.newPage();
        log.info("articlePage");
        // 隐藏webdriver特征
        articlePage.addInitScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");
    }

    /**
     * 采集列表(测试规则用)
     *
     * @param listRuleNode
     * @throws Exception
     */
    public JSONObject testGetList(ListRuleNode listRuleNode) throws Exception {
        JSONObject show = new JSONObject();
        // 测试规则专用url
        String startUrl = listRuleNode.getCheckRuleUrl();
        if (oConvertUtils.isEmpty(startUrl)) {
            throw new RuntimeException("测试页URL为空");
        }
        try {
//            playwright = Playwright.create();
//            browser = playwright.firefox().launch(
//                    new BrowserType.LaunchOptions().setHeadless(false).setDevtools(true)
//            );
            createPage();


            Response response = listPage.navigate(startUrl, new Page.NavigateOptions().setTimeout(timeout));
            if (!checkResponse(response)) {
                throw new RequestException(startUrl + "请求失败,response.status: " + response.status());
            }

            // 获取最大翻页深度(总页数)
            int totalPage = Integer.parseInt(listPage.locator(listRuleNode.getTotalPageMatch()).textContent());
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
                log.info("当前页序号: {}", currentPage);
                // 一页列表结果
                List<ListResult> resultList = new ArrayList<>();
                // 匹配区块
                List<Locator> locators = listPage.locator(listRuleNode.getPageMatch()).all();
                // 遍历每个区块
                for (Locator locator: locators) {
                    ListResult listResult = new ListResult();
                    String title = locator.locator(listRuleNode.getArticleTitleMatch()).textContent();
                    listResult.setTitle(title);
                    String date = locator.locator(listRuleNode.getArticleDateMatch()).textContent();
                    Date cutDate = DateUtils.cutDate(date);
                    listResult.setDate(cutDate);
                    String articleUrl = locator.locator(listRuleNode.getArticleUrlMatch()).getAttribute("href");
                    listResult.setUrl(articleUrl);
                    log.info("列表条目: {}", listResult.toString());
                    // 判断日期是否为目标时间段的数据
                    if (
                            ( oConvertUtils.isNotEmpty(listRuleNode.getStartTime()) && cutDate.after(listRuleNode.getStartTime()) )
                    ) {
                        log.info("设定了有效时间段,在StartTime之后");
                        if (
                                ( oConvertUtils.isNotEmpty(listRuleNode.getEndTime()) && cutDate.before(listRuleNode.getEndTime()) )
                        ) {
                            // 如果设定了起止时间,且在时间段内,则为有效数据
                            log.info("在getEndTime()之前,为有效时间段数据");
                            resultList.add(listResult);
                        } else {
                            log.info("不在getEndTime()之前,继续翻页寻找");
                        }
                    } else if (
                            oConvertUtils.isEmpty(listRuleNode.getStartTime())
                                    &&
                                    oConvertUtils.isEmpty(listRuleNode.getEndTime())
                                    &&
                                    (listRuleNode.getEffectiveDays() > 0)
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
                        log.info("超出目标时间范围,目标起始时间: {}, 目标结束时间: {}, 目标有效天数: {}, 稿件时间: {}", listRuleNode.getStartTime(), listRuleNode.getEndTime(), listRuleNode.getEffectiveDays(), cutDate);
                        if (preventToppingCount >= toppingCount) {
                            isPageDown = false;
                            break;
                        }
                        log.info("防止有置顶帖,继续处理,当前处理数量: {}, 配置容忍数量: {}", preventToppingCount, toppingCount);
                        preventToppingCount++;
                    }
                }
                show.put(showPageKey, resultList);
                show.put(showPageKey + "-" + "count", resultList.size());
                if ( isPageDown && ( (currentPage + 1) < totalPage ) ) {
                    // Thread.sleep(3*1000);
                    log.info("点击下一页");
                    listPage.locator(listRuleNode.getNextMatch()).click();
                    Thread.sleep(1*500);
                    currentPage++;
                } else {
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
        JSONObject show = new JSONObject();
        String articleUrl = articleRuleNode.getCheckRuleUrl();
        if (oConvertUtils.isEmpty(articleUrl)) {
            throw new Exception("测试页URL为空");
        }
        ArticleResult articleResult = new ArticleResult();
        try {
//            playwright = Playwright.create();
//            browser = playwright.firefox().launch(
//                    new BrowserType.LaunchOptions().setHeadless(false).setDevtools(true)
//            );
            createPage();

            // 超时重试一次
            int tries = 0;
            int tryCount = 2;
            while (tries < 2) {
                try {
                    log.info("开始采集稿件: {}, 节点规则: {}", articleUrl, articleRuleNode.toString());
                    // 打开页面
                    Response response = articlePage.navigate(articleUrl, new Page.NavigateOptions().setTimeout(timeout));
                    if (!checkResponse(response)) {
                        throw new RequestException(articleUrl + "请求失败,response.status: " + response.status());
                    }
                    // 匹配内容
                    // 判断是否为单页采集,如果为单页采集,则忽略传入的URL,使用配置中的url
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getSingleFlag()) && articleRuleNode.getSingleFlag()) {
                        articleUrl = articleRuleNode.getUrlMatch();
                    }
                    // 按配置规则匹配内容
                    articleResult.setUrl(articleUrl);
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getTitleMatch())) {
                        articleResult.setTitle(articlePage.locator(articleRuleNode.getTitleMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getSubtitleMatch())) {
                        articleResult.setSubtitle(articlePage.locator(articleRuleNode.getSubtitleMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getKeywordsMatch())) {
                        articleResult.setKeywords(articlePage.locator(articleRuleNode.getKeywordsMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getDescriptionMatch())) {
                        articleResult.setDescription(articlePage.locator(articleRuleNode.getDescriptionMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getContentMatch())) {
                        String content = new String();
                        List<Locator> contentLocatorList = articlePage.locator(articleRuleNode.getContentMatch()).all();
                        List<String> contentList = contentLocatorList.stream().map(
                                locator -> {
                                    return locator.innerHTML();
                                }
                        ).collect(Collectors.toList());
                        articleResult.setContent(String.join("", contentList));
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getDateMatch())) {
                        articleResult.setDate(articlePage.locator(articleRuleNode.getDateMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getReferenceMatch())) {
                        articleResult.setReference(articlePage.locator(articleRuleNode.getReferenceMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getSourceMatch())) {
                        articleResult.setSource(articlePage.locator(articleRuleNode.getSourceMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getAuthorMatch())) {
                        articleResult.setAuthor(articlePage.locator(articleRuleNode.getAuthorMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getVisitMatch())) {
                        articleResult.setVisit(articlePage.locator(articleRuleNode.getVisitMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getCommentMatch())) {
                        articleResult.setComment(articlePage.locator(articleRuleNode.getCommentMatch()).textContent());
                    }
                    if (oConvertUtils.isNotEmpty(articleRuleNode.getCollectMatch())) {
                        articleResult.setCollect(articlePage.locator(articleRuleNode.getCollectMatch()).textContent());
                    }
                    log.info("稿件内容采集完成: \nurl: {} \ntitle: {} \narticleResult: {}", articleResult.getUrl(), articleResult.getTitle(), articleResult.toString());
                    // 不需要处理 articleRuleNode.getCustomConfig();
                    break;
                } catch (RequestException e) {
                    log.error(e.getMessage());
                    tries++;
                    if (tries == tryCount) {
                        log.error("{},尝试请求 {} 次失败", articleUrl, tries);
                        throw e;
                    }
                    log.info("更换代理IP和UA,尝试重新请求: {}", articleUrl);
                    // 更换代理IP和UA
                    createPage();
                }
            }
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
