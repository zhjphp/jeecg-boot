package org.jeecg.modules.polymerize.playwright.exception;

import com.microsoft.playwright.TimeoutError;

/**
 * @version 1.0
 * @description: 请求错误,此错误用于需要重试的场景
 * @author: wayne
 * @date 2023/6/14 17:27
 */
public class RequestException extends TimeoutError {
    public RequestException(String message) {
        super(message);
    }

    public RequestException(String message, Throwable exception) {
        super(message, exception);
    }
}
