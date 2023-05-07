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
import org.jeecg.modules.polymerize.entity.Crawl;
import org.jeecg.modules.polymerize.service.ICrawlService;

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
 * @Description: 爬虫表
 * @Author: jeecg-boot
 * @Date:   2023-05-07
 * @Version: V1.0
 */
@Api(tags="爬虫表")
@RestController
@RequestMapping("/polymerize/crawl")
@Slf4j
public class CrawlController extends JeecgController<Crawl, ICrawlService> {
	@Autowired
	private ICrawlService crawlService;
	
	/**
	 * 分页列表查询
	 *
	 * @param crawl
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "爬虫表-分页列表查询")
	@ApiOperation(value="爬虫表-分页列表查询", notes="爬虫表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<Crawl>> queryPageList(Crawl crawl,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<Crawl> queryWrapper = QueryGenerator.initQueryWrapper(crawl, req.getParameterMap());
		Page<Crawl> page = new Page<Crawl>(pageNo, pageSize);
		IPage<Crawl> pageList = crawlService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param crawl
	 * @return
	 */
	@AutoLog(value = "爬虫表-添加")
	@ApiOperation(value="爬虫表-添加", notes="爬虫表-添加")
	@RequiresPermissions("polymerize:crawl:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody Crawl crawl) {
		crawlService.save(crawl);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param crawl
	 * @return
	 */
	@AutoLog(value = "爬虫表-编辑")
	@ApiOperation(value="爬虫表-编辑", notes="爬虫表-编辑")
	@RequiresPermissions("polymerize:crawl:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody Crawl crawl) {
		crawlService.updateById(crawl);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "爬虫表-通过id删除")
	@ApiOperation(value="爬虫表-通过id删除", notes="爬虫表-通过id删除")
	@RequiresPermissions("polymerize:crawl:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		crawlService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "爬虫表-批量删除")
	@ApiOperation(value="爬虫表-批量删除", notes="爬虫表-批量删除")
	@RequiresPermissions("polymerize:crawl:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.crawlService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "爬虫表-通过id查询")
	@ApiOperation(value="爬虫表-通过id查询", notes="爬虫表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<Crawl> queryById(@RequestParam(name="id",required=true) String id) {
		Crawl crawl = crawlService.getById(id);
		if(crawl==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(crawl);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param crawl
    */
    @RequiresPermissions("polymerize:crawl:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Crawl crawl) {
        return super.exportXls(request, crawl, Crawl.class, "爬虫表");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("polymerize:crawl:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Crawl.class);
    }

}
