package org.jeecg.modules.proxy.service.impl;

import org.jeecg.modules.proxy.service.IIPProxyService;
import org.jeecg.modules.proxy.vo.IPProxyVO;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @version 1.0
 * @description: 测试用
 * @author: wayne
 * @date 2023/6/2 14:33
 */
@Deprecated
@Service(value = "KuaiProxyService")
public class KuaiProxyService implements IIPProxyService {

    @Override
    public IPProxyVO getOne(int type) {
        IPProxyVO ipProxyVO = new IPProxyVO();
        ipProxyVO.setIP("2.2.2.2");
        ipProxyVO.setPort(2222);
        ipProxyVO.setExpireTime(new Date());
        return ipProxyVO;
    }

}
