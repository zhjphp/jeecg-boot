import com.alibaba.fastjson.JSON;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import kotlin.jvm.internal.Lambda;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.drawflow.model.ListRuleNode;
import org.jeecg.modules.polymerize.playwright.CheckRulePlaywrightCrawl;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/6/7 17:03
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = {JeecgSystemCloudApplication.class})
@Slf4j
public class PlaywrightTest {

//    @Resource
//    public IPlaywrightService playwrightService;
//
//    @Test
//    public void pa() {
//        playwrightService.test();
//    }



    public static void main(String[] args) throws Exception {

        // PlaywrightCrawl playwrightCrawl = new PlaywrightCrawl();

        // String config = "{\"drawflow\":{\"Home\":{\"data\":{\"10\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"11\"}]}},\"pos_y\":279,\"pos_x\":128,\"data\":{},\"inputs\":{},\"name\":\"StartNode\",\"html\":\"StartNode\",\"id\":10,\"class\":\"StartNode\"},\"11\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"14\"}]}},\"pos_y\":229,\"pos_x\":556,\"data\":{\"startUrls\":\"http://sd.dzwww.com/sdnews/\",\"effectiveDays\":0,\"startTime\":null,\"endTime\":null,\"pageMatch\":\"//div[@id=\\\"main2\\\"]//ul//li//div[@class=\\\"text\\\"]\",\"totalPageMatch\":\"//*[@id=\\\"flip\\\"]/a[5]\",\"nextMatch\":\"//a[contains(text(),\\\"下一页\\\")]\",\"articleUrlMatch\":\"//h3/a\",\"articleTitleMatch\":\"//h3/a\",\"articleDateMatch\":\"//p/label\"},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"10\",\"input\":\"output_1\"}]}},\"name\":\"ListRuleNode\",\"html\":\"ListRuleNode\",\"id\":11,\"class\":\"ListRuleNode\"},\"14\":{\"typenode\":\"vue\",\"outputs\":{},\"pos_y\":274,\"pos_x\":926,\"data\":{\"urlMatch\":\"no\",\"contentMatch\":\"//*[@id=\\\"news-body\\\"]\",\"dateMatch\":\"//*[@id=\\\"news-side\\\"]/div[1]\",\"keywordsMatch\":\"//html/head/meta[5]\",\"sourceMatch\":\"//*[@id=\\\"news-side\\\"]/div[2]/p\",\"singleFlag\":false,\"titleMatch\":\"//*[@id=\\\"news-head\\\"]/h2\",\"descriptionMatch\":\"//html/head/meta[6]\"},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"11\",\"input\":\"output_1\"}]}},\"name\":\"ArticleRuleNode\",\"html\":\"ArticleRuleNode\",\"id\":14,\"class\":\"ArticleRuleNode\"}}}}}";

        // playwrightCrawl.run(config);

//        CheckRulePlaywrightCrawl testRulePlaywrightCrawl = new CheckRulePlaywrightCrawl();
//        String listRuleConfig = "{\n" +
//                "  \"checkRuleUrl\": \"http://sd.dzwww.com/sdnews/\",\n" +
//                "  \"startUrls\": \"http://sd.dzwww.com/sdnews/\",\n" +
//                "  \"effectiveDays\": 0,\n" +
//                "  \"startTime\": \"2023-06-17 16:29:27\",\n" +
//                "  \"endTime\": \"2023-06-18 23:29:40\",\n" +
//                "  \"pageMatch\": \"//div[@id=\\\"main2\\\"]//ul//li//div[@class=\\\"text\\\"]\",\n" +
//                "  \"totalPageMatch\": \"//*[@id=\\\"flip\\\"]/a[5]\",\n" +
//                "  \"nextMatch\": \"//a[contains(text(),\\\"下一页\\\")]\",\n" +
//                "  \"articleUrlMatch\": \"//h3/a\",\n" +
//                "  \"articleTitleMatch\": \"//h3/a\",\n" +
//                "  \"articleDateMatch\": \"//p/label\"\n" +
//                "}";
//        ListRuleNode listRuleNode = new ListRuleNode(JSON.parseObject(listRuleConfig));
//
//        try {
//            testRulePlaywrightCrawl.testGetList(listRuleNode);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


//        Playwright playwright = Playwright.create();
//        Browser firefox = playwright.firefox().launch(
//                new BrowserType.LaunchOptions().setHeadless(false).setDevtools(true)
//        );
//        String url = "http://www.dzwww.com/xinwen/shehuixinwen/202307/t20230720_12399340.htm";
//
//        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36";
//        Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setJavaScriptEnabled(true);
//        BrowserContext chromiumContext = firefox.newContext(newContextOptions);
//
//        chromiumContext.route(Pattern.compile(".*?(\\.jpg)|(\\.JPG)|(\\.PNG)|(\\.png)|(\\.mp3)|(\\.MP3)|(\\.mp4)|(\\.MP4)|(\\.webp)|(\\.flv)|(\\.FLV).*?"), route -> route.abort());
//
//        Page page = chromiumContext.newPage();
//        page.navigate(url, new Page.NavigateOptions().setTimeout(120 * 1000));
//        log.info("打开页面");
//        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//
//        Object scrollHeight = page.evaluate(
//                "() => document.documentElement.scrollHeight"
//        );
//        log.info("document.documentElement.scrollHeight: {}", scrollHeight.toString());
//        double y = Double.parseDouble(scrollHeight.toString());
//        page.mouse().wheel(0, y);
//        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//
//        List<Locator> locators = page.locator("//div[@class='newList_content_item']").all();
//        log.info("第一页列表元素数量: {}", locators.size());
//
//        page.waitForTimeout(5000);
//        // Thread.sleep(5000);
//
//        log.info("开始第一页");
//        int i = 0;
//        for (Locator locator: locators) {
//            i++;
//            log.info("第一页，点击第 {} 篇", i);
//            Locator l = locator.locator("//div[@class='overtext']");
//            log.info("点击 {}", l.textContent());
//            l.click();
//            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//            page.waitForTimeout(5000);
//            // Thread.sleep(5000);
//            page.goBack();
//            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//            page.waitForTimeout(5000);
//            // Thread.sleep(50000);
//        }
//
//        log.info("第一页完成");
//
//        page.waitForTimeout(5000);
//        // Thread.sleep(50000);
//
//        Locator next = page.locator("//button[@class='btn-next']");
//        next.click();
//        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//        page.waitForTimeout(5000);
//        // Thread.sleep(50000);
//        log.info("点击下一页");
//
//        locators = page.locator("//div[@class='newList_content_item']").all();
//        log.info("第二页列表元素数量: {}", locators.size());
//        int j = 0;
//        for (Locator locator: locators) {
//            j++;
//            i++;
//            log.info("第二页，点击第 {} 篇", i);
//            Locator l = locator.locator("//div[@class='overtext']");
//            log.info("点击 {}", l.textContent());
//            l.click();
//            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//            page.waitForTimeout(5000);
//            // Thread.sleep(50000);
//            page.goBack();
//            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//            page.waitForTimeout(5000);
//            // Thread.sleep(50000);
//        }
//
//        log.info("第二页完成");




//        String c = page.content();
//        log.info(c);
//        Matcher m = Pattern.compile("[.\\s\\S]*来源：([\\u4e00-\\u9fa5]*)</span>").matcher(c);
//        int count = m.groupCount();
//        log.info("count: {}", count);
//        if (m.find()) {
//            log.info("1: {}", m.group(1));
//        }

//        List<Locator> ll = page.locator("//div[@class='news_left left']//li//a").all();
//        for (Locator l : ll) {
//            String y = l.getAttribute("href");
//            log.info("yyyyy: {}", y);
//            String x = l.textContent();
//            log.info(x);
//        }




//        Playwright playwright = Playwright.create();
//        Browser chromium = playwright.chromium().launch(
//                new BrowserType.LaunchOptions().setHeadless(false).setDevtools(true)
//        );
//        Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setJavaScriptEnabled(true);
//        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36";
//        newContextOptions.setUserAgent(userAgent);
//        BrowserContext chromiumContext = chromium.newContext(newContextOptions);
//
//        Page page = chromiumContext.newPage();
//        String url = "http://www.shandong.gov.cn/col/col97902/index.html";
//        double timeout = 120 * 1000;


//        page.navigate(url, new Page.NavigateOptions().setTimeout(120 * 1000));
//        String e = "function do123(as) {\n" +
//                "    console.log('hahaha'); " +
//                "    console.log(as); " +
//                "    var ans = [];\n" +
//                "    for (var i =0;i<as.length;i++){\n" +
//                "        ans.push(as[i].src);\n" +
//                "    }\n" +
//                "    return ans;\n" +
//                "}";
//        String ee = "function doit(sainan) { \n" +
//                " console.log('ai sai nan'); \n" +
//                " console.log(sainan); \n" +
//                "    var hujun = [];\n" +
//                "    for (var i =0;i<sainan.length;i++){\n" +
//                "        hujun.push(sainan[i]);\n" +
//                "    }\n" +
//                "    return hujun;\n" +
//                "}";
//        // List<String> ans = (List<String>) page.evalOnSelectorAll("li.wip_col_listli", ee);
//        List<String> hujun_sainan = page.locator("//li[@class='wip_col_listli']//a").allTextContents();
//        List<String> sainan_hujun = page.locator("//li//span").allTextContents();
//        // List<String> hujun_x_sainan = page.locator("//li[@class='wip_col_listli']//a//@href").allInnerTexts();
//        List<Locator> loves = page.locator("//li[@class='wip_col_listli']//a").all();
//        List<String> hujun_x_sainan = new ArrayList<>();
//        for (Locator love: loves) {
//            String x = love.getAttribute("href");
//            hujun_x_sainan.add(x);
//        }
//
//        List<Locator> sainan_love_hujun = page.locator("//li[@class='wip_col_listli']").all();
//
//        List<String> x_list = new ArrayList<>();
//        List<String> xx_list = new ArrayList<>();
//        List<String> xxx_list = new ArrayList<>();
//
//        for (Locator love: sainan_love_hujun) {
//            String x = love.locator("//a").textContent();
//            x_list.add(x);
//            String xx = love.locator("//span").textContent();
//            xx_list.add(xx);
//            String xxx = love.locator("//a").getAttribute("href");
//            xxx_list.add(xxx);
//        }



//        String ff = "el => el.src";
//        String ans2 = (String) page.evalOnSelector("[src]", ff);
//
//        log.info("我擦来");
//        String url2 = "http://baidu.com";
//        page.navigate(url2, new Page.NavigateOptions().setTimeout(120 * 1000));
//        log.info("关了");
//        Page page2 = chromiumContext.newPage();
//        String url3 = "http://163.com";
//        page2.navigate(url3, new Page.NavigateOptions().setTimeout(120 * 1000));
//        String content = page.content();
//        String content2 = page2.content();
//        log.info("开了");


        // mobileTest();

        // testDomainRegex();

        testDomain();

    }

    public static void lambdaTest2() {

    }

    public static void mobileTest() throws Exception {
        Playwright playwright = Playwright.create();
        Browser firefox = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setDevtools(true)
        );
        String url = "http://sgrmtwx.shouguang.gov.cn/app/index.html#/cmstopic_fyxz_sub?topicId=7cef397bab1340c6993b79d614c36e8d&VNK=cd798454&groupColumnId=426e0f36dfaf4657b5c0e428a6b67459";

        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36";
        Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setJavaScriptEnabled(true).setIsMobile(true).setViewportSize(2560, 1440).setDeviceScaleFactor(0.01);
        BrowserContext chromiumContext = firefox.newContext(newContextOptions);
        chromiumContext.setDefaultTimeout(30000);


        chromiumContext.route(Pattern.compile(".*?(\\.jpeg)|(\\.JPEG)|(\\.jpg)|(\\.JPG)|(\\.png)|(\\.PNG).*?"), route -> route.abort());

//        Locator.TextContentOptions textContentOptions = new Locator.TextContentOptions().setTimeout(30000);
//        Page.NavigateOptions navigateOptions = new Page.NavigateOptions().setTimeout(30000);

        Page page = chromiumContext.newPage();
        // page.setViewportSize(690, 1284);
        page.navigate(url, new Page.NavigateOptions().setTimeout(30 * 1000));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.waitForTimeout(5000);
//        Object scrollHeight = page.evaluate(
//                "() => document.documentElement.scrollHeight"
//        );
//        log.info("document.documentElement.scrollHeight: {}", scrollHeight.toString());
//        Integer y = Integer.parseInt(scrollHeight.toString());
        for (int i = 0; i < 10; i++) {
            log.info("移动: {}", i);
            page.mouse().move(400, 400);
            page.mouse().down();
            page.mouse().move(400, 200);
            page.mouse().up();
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.waitForTimeout(2000);
        }

        log.info("开始定位列表");
        List<Locator> locators = page.locator("xpath=//div[@class='list list-content']").all();
        log.info("定位列表完成, {}", locators.size());
        for (Locator locator : locators) {
            String title = locator.locator("xpath=//div[@class='video-title']").textContent();
            log.info("点击: {}", title);
            locator.click();
            log.info("进入页面");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            String tmp = page.locator("xpath=//div[@class='content-hot-wrap']//div[1]//div[1]").textContent();
            log.info("页面内容: {}", tmp);
            page.waitForTimeout(2000);
            page.goBack();
            log.info("返回列表");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.waitForTimeout(2000);
        }
    }

    public static void testDomainRegex() {
        String a = "c-c_c.ddd.com";
        String reg = "[a-zA-Z0-9\\-_]+.ddd.com";
        log.info("result: {}", a.matches(reg));
    }

    public static void testDomain() throws Exception {
        URL urlObj = new URL("https://1_--1ccasd.ddd.ccc.www.google.com/search?q=%E7%BF%BB%E8%AF%91&oq=%E7%BF%BB%E8%AF%91&gs_lcrp=EgZjaHJvbWUqBggAEEUYOzIGCAAQRRg7MgcIARAAGIAEMgcIAhAAGIAEMgYIAxBFGD0yBggEEEUYQTIGCAUQRRg9MgYIBhBFGEEyBggHEEUYQdIBCDIwNzFqMGo3qAIAsAIA&sourceid=chrome&ie=UTF-8");
        log.info(urlObj.getHost());
    }

    public void regexRouteTest() {
        Playwright playwright = Playwright.create();
        Browser firefox = playwright.firefox().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setDevtools(true)
        );
        String url = "http://www.dzwww.com/xinwen/shehuixinwen/202307/t20230720_12399340.htm";

        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36";
        Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setJavaScriptEnabled(true);
        BrowserContext chromiumContext = firefox.newContext(newContextOptions);

        chromiumContext.route(Pattern.compile(".*?(\\.jpg)|(\\.JPG)|(\\.PNG)|(\\.png)|(\\.mp3)|(\\.MP3)|(\\.mp4)|(\\.MP4)|(\\.webp)|(\\.flv)|(\\.FLV).*?"), route -> route.abort());

        Page page = chromiumContext.newPage();
        page.navigate(url, new Page.NavigateOptions().setTimeout(120 * 1000));
    }





}
