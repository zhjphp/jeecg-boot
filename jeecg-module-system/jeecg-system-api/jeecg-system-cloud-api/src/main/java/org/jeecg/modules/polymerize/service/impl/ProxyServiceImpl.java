package org.jeecg.modules.polymerize.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.polymerize.constant.PolymerizeCacheConstant;
import org.jeecg.modules.polymerize.entity.Proxy;
import org.jeecg.modules.polymerize.mapper.ProxyMapper;
import org.jeecg.modules.polymerize.service.IProxyService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: ip代理
 * @Author: jeecg-boot
 * @Date:   2023-06-01
 * @Version: V1.0
 */
@Service
public class ProxyServiceImpl extends ServiceImpl<ProxyMapper, Proxy> implements IProxyService {

    @Resource
    ProxyMapper proxyMapper;

    /**
     * 获取代理列表
     *
     * @param type 代理服务类型
     * @return
     */
    @Override
    @Cacheable(value=PolymerizeCacheConstant.POLYMERIZE_IP_PROXY_CACHE, key="#type")
    public LinkedList<Proxy> getProxyList(int type) {
        LambdaQueryWrapper<Proxy> query = new LambdaQueryWrapper<>();
        query.eq(Proxy::getType, type);
        query.eq(Proxy::getStatus, Proxy.STATUS_ENABLE);
        query.orderByDesc(Proxy::getRank);
        LinkedList<Proxy> proxyList = proxyMapper.selectList(query).stream().collect(Collectors.toCollection(LinkedList::new));
        return proxyList;
    }

}
