package org.jeecg.modules.polymerize.playwright.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0
 * @description: PlaywrightBrowserConfig
 * @author: wayne
 * @date 2023/6/5 15:29
 */
@Configuration
public class PlaywrightBrowserConfig {

    private final Playwright playwright;

    @Value("${polymerize.playwright.enableHeadless}")
    private boolean enableHeadless;

    @Value("${polymerize.playwright.browserType}")
    private String browserType;

    public PlaywrightBrowserConfig(Playwright playwright) {
        this.playwright = playwright;
    }

    @Bean(destroyMethod = "close")
    public Browser getChromiumBrowser() {
        Browser browser = null;
        if (browserType.equals("chromium")) {
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(enableHeadless)
            );
        }
        if (browserType.equals("firefox")) {
            browser = playwright.firefox().launch(
                    new BrowserType.LaunchOptions().setHeadless(enableHeadless)
            );
        }
        if (browserType.equals("webkit")) {
            browser = playwright.webkit().launch(
                    new BrowserType.LaunchOptions().setHeadless(enableHeadless)
            );
        }
        return browser;
    }

}
