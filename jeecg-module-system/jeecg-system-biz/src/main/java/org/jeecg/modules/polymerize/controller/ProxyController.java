package org.jeecg.modules.polymerize.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.constant.PolymerizeCacheConstant;
import org.jeecg.modules.polymerize.entity.Proxy;
import org.jeecg.modules.polymerize.service.IProxyService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * @Description: ip代理
 * @Author: jeecg-boot
 * @Date:   2023-06-01
 * @Version: V1.0
 */
@Api(tags="ip代理")
@RestController
@RequestMapping("/polymerize/proxy")
@Slf4j
public class ProxyController extends JeecgController<Proxy, IProxyService> {
	@Autowired
	private IProxyService proxyService;
	
	/**
	 * 分页列表查询
	 *
	 * @param proxy
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "ip代理-分页列表查询")
	@ApiOperation(value="ip代理-分页列表查询", notes="ip代理-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<Proxy>> queryPageList(Proxy proxy,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<Proxy> queryWrapper = QueryGenerator.initQueryWrapper(proxy, req.getParameterMap());
		Page<Proxy> page = new Page<Proxy>(pageNo, pageSize);
		IPage<Proxy> pageList = proxyService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param proxy
	 * @return
	 */
	@CacheEvict(value=PolymerizeCacheConstant.POLYMERIZE_IP_PROXY_CACHE, allEntries=true)
	@AutoLog(value = "ip代理-添加")
	@ApiOperation(value="ip代理-添加", notes="ip代理-添加")
	@RequiresPermissions("polymerize:proxy:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody Proxy proxy) {
		proxyService.save(proxy);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param proxy
	 * @return
	 */
	@CacheEvict(value=PolymerizeCacheConstant.POLYMERIZE_IP_PROXY_CACHE, allEntries=true)
	@AutoLog(value = "ip代理-编辑")
	@ApiOperation(value="ip代理-编辑", notes="ip代理-编辑")
	@RequiresPermissions("polymerize:proxy:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody Proxy proxy) {
		proxyService.updateById(proxy);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@CacheEvict(value=PolymerizeCacheConstant.POLYMERIZE_IP_PROXY_CACHE, allEntries=true)
	@AutoLog(value = "ip代理-通过id删除")
	@ApiOperation(value="ip代理-通过id删除", notes="ip代理-通过id删除")
	@RequiresPermissions("polymerize:proxy:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		proxyService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@CacheEvict(value=PolymerizeCacheConstant.POLYMERIZE_IP_PROXY_CACHE, allEntries=true)
	@AutoLog(value = "ip代理-批量删除")
	@ApiOperation(value="ip代理-批量删除", notes="ip代理-批量删除")
	@RequiresPermissions("polymerize:proxy:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.proxyService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "ip代理-通过id查询")
	@ApiOperation(value="ip代理-通过id查询", notes="ip代理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<Proxy> queryById(@RequestParam(name="id",required=true) String id) {
		Proxy proxy = proxyService.getById(id);
		if(proxy==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(proxy);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param proxy
    */
    @RequiresPermissions("polymerize:proxy:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Proxy proxy) {
        return super.exportXls(request, proxy, Proxy.class, "ip代理");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("polymerize:proxy:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Proxy.class);
    }

}
