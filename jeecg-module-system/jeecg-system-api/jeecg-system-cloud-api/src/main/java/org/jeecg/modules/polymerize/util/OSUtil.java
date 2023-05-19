package org.jeecg.modules.polymerize.util;

import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/5/17 17:13
 */
@Component
public class OSUtil {

    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        if (os != null) {
            os = os.toLowerCase();
            if (os.contains("windows")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLinux() {
        String os = System.getProperty("os.name");
        if (os != null) {
            os = os.toLowerCase();
            if (os.contains("linux")) {
                return true;
            }
        }
        return false;
    }

}
