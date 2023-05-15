package org.jeecg.modules.polymerize.exception;

/**
 * @version 1.0
 * @description: Polymerize模块自定义异常
 * @author: wayne
 * @date 2023/5/15 11:15
 */
public class PolymerizeException extends RuntimeException {

    protected String value;

    public PolymerizeException(String message) {
        super(message);
    }

    public PolymerizeException(String message, String value) {
        super(message);
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
