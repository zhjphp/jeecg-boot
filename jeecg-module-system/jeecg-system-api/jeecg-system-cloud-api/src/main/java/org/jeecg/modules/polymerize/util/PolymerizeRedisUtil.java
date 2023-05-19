package org.jeecg.modules.polymerize.util;

import org.jeecg.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @description: 扩展RedisUtil
 * @author: wayne
 * @date 2023/5/15 14:32
 */
@Component
public class PolymerizeRedisUtil extends RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public PolymerizeRedisUtil() {
        super();
    }

    /**
     * list-leftPush
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lPush(String key, Object value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * list-leftPush
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lPush(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * list-rightPop
     *
     * @param key   键
     * @return
     */
    public Object lPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

}
