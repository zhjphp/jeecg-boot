package org.jeecg.modules.proxy.vo;

import lombok.Data;

import java.util.Date;

/**
 * @version 1.0
 * @description: TODO
 * @author: wayne
 * @date 2023/6/1 21:50
 */
@Data
public class IPProxyVO {

    /**代理地址*/
    public String ip;

    /**代理端口*/
    public int port;

    /**代理失效时间*/
    public Date expireTime;

    /**访问协议*/
    public String scheme;

}
