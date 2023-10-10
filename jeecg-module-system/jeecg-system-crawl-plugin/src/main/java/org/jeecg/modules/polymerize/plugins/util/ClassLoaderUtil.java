package org.jeecg.modules.polymerize.plugins.util;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @version 1.0
 * @description: Class加载器
 * @author: wayne
 * @date 2023/9/4 17:01
 */
@Slf4j
public class ClassLoaderUtil {

    public static ClassLoader getClassLoader(String urlStr) {
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{ new URL(urlStr) }, ClassLoaderUtil.class.getClassLoader());
            return urlClassLoader;
        } catch (Exception e) {
            log.error("getClassLoader-error", e);
            return null;
        }
    }

}
