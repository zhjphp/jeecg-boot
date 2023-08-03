package org.jeecg.modules.taskjob.service.impl;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.modules.polymerize.util.PolymerizeRedisUtil;
import org.jeecg.modules.taskjob.service.IAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @description: feign验证
 * @author: wayne
 * @date 2023/5/23 10:25
 */
@RefreshScope
@Component
public class AuthServiceImpl implements IAuthService {

    @Value("${polymerize.feign.username}")
    private String username;

    @Value("${polymerize.feign.password}")
    private String password;

    @Value("${polymerize.feign.expire}")
    private long expire;

    /**
     * 获取临时令牌
     * 模拟登陆接口，获取模拟 Token
     *
     * @return String
     */
    @Override
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
