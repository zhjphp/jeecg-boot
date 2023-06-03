package org.jeecg.modules.polymerize.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.polymerize.entity.Proxy;

import java.util.LinkedList;
import java.util.List;

/**
 * @Description: ip代理
 * @Author: jeecg-boot
 * @Date:   2023-06-01
 * @Version: V1.0
 */
public interface IProxyService extends IService<Proxy> {

    /**
     * 获取代理列表
     *
     * @param type 代理服务类型
     * @return
     */
    LinkedList<Proxy> getProxyList(int type);

}
