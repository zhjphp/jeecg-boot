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
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.dto.TmpCrawlDataDTO;
import org.jeecg.modules.polymerize.entity.TmpCrawlData;
import org.jeecg.modules.polymerize.service.ITmpCrawlDataService;

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
 * @Description: 爬虫临时数据存储
 * @Author: jeecg-boot
 * @Date:   2023-06-30
 * @Version: V1.0
 */
@Api(tags="爬虫临时数据存储")
@RestController
@RequestMapping("/polymerize/tmpCrawlData")
@Slf4j
public class TmpCrawlDataController extends JeecgController<TmpCrawlData, ITmpCrawlDataService> {
	@Autowired
	private ITmpCrawlDataService tmpCrawlDataService;
	
	/**
	 * 分页列表查询
	 *
	 * @param tmpCrawlData
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "爬虫临时数据存储-分页列表查询")
	@ApiOperation(value="爬虫临时数据存储-分页列表查询", notes="爬虫临时数据存储-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<TmpCrawlData>> queryPageList(TmpCrawlData tmpCrawlData,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<TmpCrawlData> queryWrapper = QueryGenerator.initQueryWrapper(tmpCrawlData, req.getParameterMap());
		Page<TmpCrawlData> page = new Page<TmpCrawlData>(pageNo, pageSize);
		IPage<TmpCrawlData> pageList = tmpCrawlDataService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param tmpCrawlData
	 * @return
	 */
	@AutoLog(value = "爬虫临时数据存储-添加")
	@ApiOperation(value="爬虫临时数据存储-添加", notes="爬虫临时数据存储-添加")
	@RequiresPermissions("polymerize:tmp_crawl_data:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody TmpCrawlData tmpCrawlData) {
		tmpCrawlDataService.save(tmpCrawlData);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param tmpCrawlData
	 * @return
	 */
	@AutoLog(value = "爬虫临时数据存储-编辑")
	@ApiOperation(value="爬虫临时数据存储-编辑", notes="爬虫临时数据存储-编辑")
	@RequiresPermissions("polymerize:tmp_crawl_data:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody TmpCrawlData tmpCrawlData) {
		tmpCrawlDataService.updateById(tmpCrawlData);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "爬虫临时数据存储-通过id删除")
	@ApiOperation(value="爬虫临时数据存储-通过id删除", notes="爬虫临时数据存储-通过id删除")
	@RequiresPermissions("polymerize:tmp_crawl_data:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		tmpCrawlDataService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "爬虫临时数据存储-批量删除")
	@ApiOperation(value="爬虫临时数据存储-批量删除", notes="爬虫临时数据存储-批量删除")
	@RequiresPermissions("polymerize:tmp_crawl_data:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.tmpCrawlDataService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "爬虫临时数据存储-通过id查询")
	@ApiOperation(value="爬虫临时数据存储-通过id查询", notes="爬虫临时数据存储-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TmpCrawlData> queryById(@RequestParam(name="id",required=true) String id) {
		TmpCrawlData tmpCrawlData = tmpCrawlDataService.getById(id);
		if(tmpCrawlData==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tmpCrawlData);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param tmpCrawlData
    */
    @RequiresPermissions("polymerize:tmp_crawl_data:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, TmpCrawlData tmpCrawlData) {
        return super.exportXls(request, tmpCrawlData, TmpCrawlData.class, "爬虫临时数据存储");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("polymerize:tmp_crawl_data:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, TmpCrawlData.class);
    }

}
