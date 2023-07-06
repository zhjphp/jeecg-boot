package org.jeecg.modules.polymerize.playwright.data;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.config.mqtoken.UserTokenContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.polymerize.api.IPolymerizeAPI;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import org.jeecg.modules.polymerize.util.PolymerizeRedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/6/30 16:24
 */
@Slf4j
@Component
public class DataStorageService {

    @Value("${polymerize.feign.username}")
    private String username;

    @Value("${polymerize.feign.password}")
    private String password;

    @Value("${polymerize.feign.expire}")
    private long expire;

    @Resource
    private IPolymerizeAPI polymerizeAPI;

    public boolean addTmpCrawlData(TmpCrawlData tmpCrawlData) {
        // 设置线程会话Token
        UserTokenContext.setToken(getTemporaryToken());
        // log.info("写入数据: {}", tmpCrawlData.toString());
        return polymerizeAPI.addTmpCrawlData(tmpCrawlData);
    }

    public String getTemporaryToken() {
        PolymerizeRedisUtil polymerizeRedisUtil = SpringContextUtils.getBean(PolymerizeRedisUtil.class);
        // 模拟登录生成Token
        String token = JwtUtil.sign(username, password);
        // 设置Token缓存有效时间为 5 分钟
        polymerizeRedisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        polymerizeRedisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, expire);
        return token;
    }

}
