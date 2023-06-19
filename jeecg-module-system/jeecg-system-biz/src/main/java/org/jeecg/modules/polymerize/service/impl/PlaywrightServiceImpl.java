package org.jeecg.modules.polymerize.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.service.IPlaywrightService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/6/5 14:22
 */
@Slf4j
@Component
public class PlaywrightServiceImpl implements IPlaywrightService {

    @Override
    public void test() {

    }

}
