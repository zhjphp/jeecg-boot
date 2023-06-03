package org.jeecg.modules.proxy.service;

import org.jeecg.modules.proxy.vo.IPProxyVO;

public interface IIPProxyService {

    /**
     * 获取一个IP
     *
     * @param type IP类型
     * @return
     * @throws Exception
     */
    IPProxyVO getOne(int type) throws Exception ;

}
