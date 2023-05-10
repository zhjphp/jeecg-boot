package org.jeecg.modules.polymerize.controller;

import java.util.ArrayList;
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
import org.jeecg.modules.polymerize.entity.InformationSourceTask;
import org.jeecg.modules.polymerize.service.ICrawlService;
import org.jeecg.modules.polymerize.service.IInformationSourceTaskService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.polymerize.vo.InformationSourceTaskVO;
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
 * @Description: 信源任务
 * @Author: jeecg-boot
 * @Date:   2023-05-07
 * @Version: V1.0
 */
@Api(tags="信源任务")
@RestController
@RequestMapping("/polymerize/informationSourceTask")
@Slf4j
public class InformationSourceTaskController extends JeecgController<InformationSourceTask, IInformationSourceTaskService> {
	@Autowired
	private IInformationSourceTaskService informationSourceTaskService;

	 @Autowired
	 private ICrawlService crawlService;
	
	/**
	 * 分页列表查询
	 *
	 * @param informationSourceTask
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "信源任务-分页列表查询")
	@ApiOperation(value="信源任务-分页列表查询", notes="信源任务-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<InformationSourceTaskVO>> queryPageList(InformationSourceTask informationSourceTask,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<InformationSourceTask> queryWrapper = QueryGenerator.initQueryWrapper(informationSourceTask, req.getParameterMap());
		Page<InformationSourceTask> page = new Page<InformationSourceTask>(pageNo, pageSize);
		IPage<InformationSourceTask> pageList = informationSourceTaskService.page(page, queryWrapper);
		// 为返回内容添加爬虫名称,再查一遍爬虫表,不再去写sql语句
		List<InformationSourceTaskVO> list = new ArrayList<InformationSourceTaskVO>();
		for (InformationSourceTask item: pageList.getRecords()) {
			InformationSourceTaskVO tmp = new InformationSourceTaskVO(item);
			// 查询任务指定的爬虫名称
			Crawl crawl = crawlService.getById(tmp.getCrawlId());
			tmp.setCrawlName(crawl.getName());
			list.add(tmp);
		}
		Page<InformationSourceTaskVO> voPage = new Page<InformationSourceTaskVO>(pageNo, pageSize);
		IPage<InformationSourceTaskVO> voPageList = voPage.setPages(pageList.getPages());
		voPageList.setTotal(pageList.getTotal());
		voPageList.setSize(pageList.getSize());
		voPageList.setCurrent(pageList.getCurrent());
		voPageList.setRecords(list);
		return Result.OK(voPageList);
	}
	
	/**
	 *   添加
	 *
	 * @param informationSourceTask
	 * @return
	 */
	@AutoLog(value = "信源任务-添加")
	@ApiOperation(value="信源任务-添加", notes="信源任务-添加")
	@RequiresPermissions("polymerize:information_source_task:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody InformationSourceTask informationSourceTask) {
		informationSourceTaskService.save(informationSourceTask);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param informationSourceTask
	 * @return
	 */
	@AutoLog(value = "信源任务-编辑")
	@ApiOperation(value="信源任务-编辑", notes="信源任务-编辑")
	@RequiresPermissions("polymerize:information_source_task:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody InformationSourceTask informationSourceTask) {
		informationSourceTaskService.updateById(informationSourceTask);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "信源任务-通过id删除")
	@ApiOperation(value="信源任务-通过id删除", notes="信源任务-通过id删除")
	@RequiresPermissions("polymerize:information_source_task:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		informationSourceTaskService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "信源任务-批量删除")
	@ApiOperation(value="信源任务-批量删除", notes="信源任务-批量删除")
	@RequiresPermissions("polymerize:information_source_task:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.informationSourceTaskService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "信源任务-通过id查询")
	@ApiOperation(value="信源任务-通过id查询", notes="信源任务-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<InformationSourceTask> queryById(@RequestParam(name="id",required=true) String id) {
		InformationSourceTask informationSourceTask = informationSourceTaskService.getById(id);
		if(informationSourceTask==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(informationSourceTask);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param informationSourceTask
    */
    @RequiresPermissions("polymerize:information_source_task:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, InformationSourceTask informationSourceTask) {
        return super.exportXls(request, informationSourceTask, InformationSourceTask.class, "信源任务");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("polymerize:information_source_task:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, InformationSourceTask.class);
    }

}
