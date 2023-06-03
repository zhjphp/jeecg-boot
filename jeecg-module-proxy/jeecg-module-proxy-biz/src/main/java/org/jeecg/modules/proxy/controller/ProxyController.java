package org.jeecg.modules.proxy.controller;

import io.swagger.annotations.Api;
import org.apache.ibatis.annotations.Param;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.entity.Proxy;
import org.jeecg.modules.polymerize.service.IProxyService;
import org.jeecg.modules.proxy.service.IIPProxyService;
import org.jeecg.modules.proxy.vo.IPProxyVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.LinkedList;

@Api(tags = "ip代理")
@RestController
@RequestMapping("/proxy")
@Slf4j
public class ProxyController {

	@Resource
	IProxyService proxyService;

	/**
	 * 获取一个IP
	 *
	 * @param type ip类型
	 * @return
	 */
	@GetMapping("/getOne")
	public Result<IPProxyVO> getOne(@Param("type") int type) {
		// 获取代理类
		LinkedList<Proxy> proxyList = proxyService.getProxyList(type);
		if (oConvertUtils.listIsEmpty(proxyList)) {
			return Result.error("没有找到可用代理");
		}
		IPProxyVO ipProxyVO = null;
		try {
			while (oConvertUtils.isEmpty(ipProxyVO)) {
				Proxy proxy = proxyList.pop();
				if (oConvertUtils.isNotEmpty(proxy)) {
					// 实例化代理类
					IIPProxyService ipProxyService = (IIPProxyService) SpringContextUtils.getApplicationContext().getBean(proxy.getBeanName());
					// 执行方法
					ipProxyVO = ipProxyService.getOne(type);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("获取代理IP错误: " + e.getMessage() + ", 详情: " + e.toString());
		}
		if (oConvertUtils.isEmpty(ipProxyVO)) {
			return Result.error("所有代理不可用");
		}
		return Result.OK(ipProxyVO);
	}
}
