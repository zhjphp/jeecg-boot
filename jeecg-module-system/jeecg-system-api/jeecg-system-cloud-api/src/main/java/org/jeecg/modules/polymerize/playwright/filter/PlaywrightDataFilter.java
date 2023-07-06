package org.jeecg.modules.polymerize.playwright.filter;

import org.jeecg.common.util.oConvertUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

/**
 * @version 1.0
 * @description: 处理爬虫数据
 * @author: wayne
 * @date 2023/6/29 16:06
 */
public class PlaywrightDataFilter {

    /**
     * 过滤稿件详情
     * @param content
     * @param baseUri
     * @return String
     */
    public static String filterArticleContent(String content, String baseUri) {
        if (oConvertUtils.isEmpty(content)) {
            return null;
        }
        Safelist safelist = Safelist.none().addTags("p","img").addAttributes("img", "src").addProtocols("img", "src", "http", "https");
        return Jsoup.clean(content, baseUri, safelist);
    }

    /**
     * 过滤除详情外的其他数据
     * @param content
     * @return String
     */
    public static String filterString(String content) {
        if (oConvertUtils.isEmpty(content)) {
            return null;
        }
        Safelist safelist = Safelist.none();
        return new Cleaner(safelist).clean(Jsoup.parse(content)).text();
    }

}
