import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.polymerize.drawflow.model.ListRuleNode;
import org.jeecg.modules.polymerize.playwright.CheckRulePlaywrightCrawl;

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



    public static void main(String[] args) {

        // PlaywrightCrawl playwrightCrawl = new PlaywrightCrawl();

        // String config = "{\"drawflow\":{\"Home\":{\"data\":{\"10\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"11\"}]}},\"pos_y\":279,\"pos_x\":128,\"data\":{},\"inputs\":{},\"name\":\"StartNode\",\"html\":\"StartNode\",\"id\":10,\"class\":\"StartNode\"},\"11\":{\"typenode\":\"vue\",\"outputs\":{\"output_1\":{\"connections\":[{\"output\":\"input_1\",\"node\":\"14\"}]}},\"pos_y\":229,\"pos_x\":556,\"data\":{\"startUrls\":\"http://sd.dzwww.com/sdnews/\",\"effectiveDays\":0,\"startTime\":null,\"endTime\":null,\"pageMatch\":\"//div[@id=\\\"main2\\\"]//ul//li//div[@class=\\\"text\\\"]\",\"totalPageMatch\":\"//*[@id=\\\"flip\\\"]/a[5]\",\"nextMatch\":\"//a[contains(text(),\\\"下一页\\\")]\",\"articleUrlMatch\":\"//h3/a\",\"articleTitleMatch\":\"//h3/a\",\"articleDateMatch\":\"//p/label\"},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"10\",\"input\":\"output_1\"}]}},\"name\":\"ListRuleNode\",\"html\":\"ListRuleNode\",\"id\":11,\"class\":\"ListRuleNode\"},\"14\":{\"typenode\":\"vue\",\"outputs\":{},\"pos_y\":274,\"pos_x\":926,\"data\":{\"urlMatch\":\"no\",\"contentMatch\":\"//*[@id=\\\"news-body\\\"]\",\"dateMatch\":\"//*[@id=\\\"news-side\\\"]/div[1]\",\"keywordsMatch\":\"//html/head/meta[5]\",\"sourceMatch\":\"//*[@id=\\\"news-side\\\"]/div[2]/p\",\"singleFlag\":false,\"titleMatch\":\"//*[@id=\\\"news-head\\\"]/h2\",\"descriptionMatch\":\"//html/head/meta[6]\"},\"inputs\":{\"input_1\":{\"connections\":[{\"node\":\"11\",\"input\":\"output_1\"}]}},\"name\":\"ArticleRuleNode\",\"html\":\"ArticleRuleNode\",\"id\":14,\"class\":\"ArticleRuleNode\"}}}}}";

        // playwrightCrawl.run(config);

        CheckRulePlaywrightCrawl testRulePlaywrightCrawl = new CheckRulePlaywrightCrawl();
        String listRuleConfig = "{\n" +
                "  \"checkRuleUrl\": \"http://sd.dzwww.com/sdnews/\",\n" +
                "  \"startUrls\": \"http://sd.dzwww.com/sdnews/\",\n" +
                "  \"effectiveDays\": 0,\n" +
                "  \"startTime\": \"2023-06-17 16:29:27\",\n" +
                "  \"endTime\": \"2023-06-18 23:29:40\",\n" +
                "  \"pageMatch\": \"//div[@id=\\\"main2\\\"]//ul//li//div[@class=\\\"text\\\"]\",\n" +
                "  \"totalPageMatch\": \"//*[@id=\\\"flip\\\"]/a[5]\",\n" +
                "  \"nextMatch\": \"//a[contains(text(),\\\"下一页\\\")]\",\n" +
                "  \"articleUrlMatch\": \"//h3/a\",\n" +
                "  \"articleTitleMatch\": \"//h3/a\",\n" +
                "  \"articleDateMatch\": \"//p/label\"\n" +
                "}";
        ListRuleNode listRuleNode = new ListRuleNode(JSON.parseObject(listRuleConfig));

        try {
            testRulePlaywrightCrawl.testGetList(listRuleNode);
        } catch (Exception e) {
            e.printStackTrace();
        }




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




    }





}
