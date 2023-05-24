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
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.modules.polymerize.entity.PolymerizeCategory;
import org.jeecg.modules.polymerize.service.IPolymerizeCategoryService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.service.ISysCategoryService;
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
 * @Description: 总分类树
 * @Author: jeecg-boot
 * @Date:   2023-04-20
 * @Version: V1.0
 */
@Api(tags="总分类树")
@RestController
@RequestMapping("/polymerize/category")
@Slf4j
public class PolymerizeCategoryController extends JeecgController<PolymerizeCategory, IPolymerizeCategoryService>{
	@Autowired
	private IPolymerizeCategoryService polymerizeCategoryService;

	/**
	 * 分页列表查询
	 *
	 * @param polymerizeCategory
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "总分类树-分页列表查询")
	@ApiOperation(value="总分类树-分页列表查询", notes="总分类树-分页列表查询")
	@GetMapping(value = "/rootList")
	public Result<IPage<PolymerizeCategory>> queryPageList(PolymerizeCategory polymerizeCategory,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		String hasQuery = req.getParameter("hasQuery");
        if(hasQuery != null && "true".equals(hasQuery)){
            QueryWrapper<PolymerizeCategory> queryWrapper =  QueryGenerator.initQueryWrapper(polymerizeCategory, req.getParameterMap());
            List<PolymerizeCategory> list = polymerizeCategoryService.queryTreeListNoPage(queryWrapper);
            IPage<PolymerizeCategory> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        }else{
            String parentId = polymerizeCategory.getPid();
            if (oConvertUtils.isEmpty(parentId)) {
                parentId = "0";
            }
            polymerizeCategory.setPid(null);
            QueryWrapper<PolymerizeCategory> queryWrapper = QueryGenerator.initQueryWrapper(polymerizeCategory, req.getParameterMap());
            // 使用 eq 防止模糊查询
            queryWrapper.eq("pid", parentId);
            Page<PolymerizeCategory> page = new Page<PolymerizeCategory>(pageNo, pageSize);
            IPage<PolymerizeCategory> pageList = polymerizeCategoryService.page(page, queryWrapper);
            return Result.OK(pageList);
        }
	}

	 /**
	  * 【vue3专用】加载节点的子数据
	  *
	  * @param pid
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
	 public Result<List<SelectTreeModel>> loadTreeChildren(@RequestParam(name = "pid") String pid) {
		 Result<List<SelectTreeModel>> result = new Result<>();
		 try {
			 List<SelectTreeModel> ls = polymerizeCategoryService.queryListByPid(pid);
			 result.setResult(ls);
			 result.setSuccess(true);
		 } catch (Exception e) {
			 e.printStackTrace();
			 result.setMessage(e.getMessage());
			 result.setSuccess(false);
		 }
		 return result;
	 }

	 /**
	  * 【vue3专用】加载一级节点/如果是同步 则所有数据
	  *
	  * @param async
	  * @param pid
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
	 public Result<List<SelectTreeModel>> loadTreeRoot(@RequestParam(name = "async") Boolean async, @RequestParam(name = "pid") String pid) {
		 Result<List<SelectTreeModel>> result = new Result<>();
		 try {
			 // List<SelectTreeModel> ls = polymerizeCategoryService.queryListByCode(pcode);
			 List<SelectTreeModel> ls = polymerizeCategoryService.queryListByPid(pid);
			 if (!async) {
				 loadAllChildren(ls);
			 }
			 result.setResult(ls);
			 result.setSuccess(true);
		 } catch (Exception e) {
			 e.printStackTrace();
			 result.setMessage(e.getMessage());
			 result.setSuccess(false);
		 }
		 return result;
	 }

	 /**
	  * 【vue3专用】加载一级节点/如果是同步 则所有数据
	  *
	  * @param pid
	  * @param condition
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeListByPidCondition", method = RequestMethod.GET)
	 public Result<List<SelectTreeModel>> loadTreeListByPidCondition(@RequestParam(name = "pid") String pid, @RequestParam(name="condition",required = false) String condition) {
		 Result<List<SelectTreeModel>> result = new Result<List<SelectTreeModel>>();
		 Map<String, String> query = null;
		 if(oConvertUtils.isNotEmpty(condition)) {
			 query = JSON.parseObject(condition, Map.class);
		 }
		 List<SelectTreeModel> ls = polymerizeCategoryService.queryListByPidCondition(pid, query);
		 result.setSuccess(true);
		 result.setResult(ls);
		 return result;
	 }

	 /**
	  * 【vue3专用】递归求子节点 同步加载用到
	  *
	  * @param ls
	  */
	 private void loadAllChildren(List<SelectTreeModel> ls) {
		 for (SelectTreeModel tsm : ls) {
			 List<SelectTreeModel> temp = polymerizeCategoryService.queryListByPid(tsm.getKey());
			 if (temp != null && temp.size() > 0) {
				 tsm.setChildren(temp);
				 loadAllChildren(temp);
			 }
		 }
	 }

	 /**
      * 获取子数据
      * @param polymerizeCategory
      * @param req
      * @return
      */
	//@AutoLog(value = "总分类树-获取子数据")
	@ApiOperation(value="总分类树-获取子数据", notes="总分类树-获取子数据")
	@GetMapping(value = "/childList")
	public Result<IPage<PolymerizeCategory>> queryPageList(PolymerizeCategory polymerizeCategory,HttpServletRequest req) {
		QueryWrapper<PolymerizeCategory> queryWrapper = QueryGenerator.initQueryWrapper(polymerizeCategory, req.getParameterMap());
		queryWrapper.orderByDesc("rank");
		List<PolymerizeCategory> list = polymerizeCategoryService.list(queryWrapper);
		IPage<PolymerizeCategory> pageList = new Page<>(1, 10, list.size());
        pageList.setRecords(list);
		return Result.OK(pageList);
	}

    /**
      * 批量查询子节点
      * @param parentIds 父ID（多个采用半角逗号分割）
      * @return 返回 IPage
      * @param parentIds
      * @return
      */
	//@AutoLog(value = "总分类树-批量获取子数据")
    @ApiOperation(value="总分类树-批量获取子数据", notes="总分类树-批量获取子数据")
    @GetMapping("/getChildListBatch")
    public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
        try {
            QueryWrapper<PolymerizeCategory> queryWrapper = new QueryWrapper<>();
            List<String> parentIdList = Arrays.asList(parentIds.split(","));
            queryWrapper.in("pid", parentIdList);
            List<PolymerizeCategory> list = polymerizeCategoryService.list(queryWrapper);
            IPage<PolymerizeCategory> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("批量查询子节点失败：" + e.getMessage());
        }
    }
	
	/**
	 *   添加
	 *
	 * @param polymerizeCategory
	 * @return
	 */
	@AutoLog(value = "总分类树-添加")
	@ApiOperation(value="总分类树-添加", notes="总分类树-添加")
    @RequiresPermissions("polymerize:polymerize_category:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody PolymerizeCategory polymerizeCategory) {
		polymerizeCategoryService.addPolymerizeCategory(polymerizeCategory);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param polymerizeCategory
	 * @return
	 */
	@AutoLog(value = "总分类树-编辑")
	@ApiOperation(value="总分类树-编辑", notes="总分类树-编辑")
    @RequiresPermissions("polymerize:polymerize_category:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody PolymerizeCategory polymerizeCategory) {
		polymerizeCategoryService.updatePolymerizeCategory(polymerizeCategory);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "总分类树-通过id删除")
	@ApiOperation(value="总分类树-通过id删除", notes="总分类树-通过id删除")
    @RequiresPermissions("polymerize:polymerize_category:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		polymerizeCategoryService.deletePolymerizeCategory(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "总分类树-批量删除")
	@ApiOperation(value="总分类树-批量删除", notes="总分类树-批量删除")
    @RequiresPermissions("polymerize:polymerize_category:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.polymerizeCategoryService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "总分类树-通过id查询")
	@ApiOperation(value="总分类树-通过id查询", notes="总分类树-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<PolymerizeCategory> queryById(@RequestParam(name="id",required=true) String id) {
		PolymerizeCategory polymerizeCategory = polymerizeCategoryService.getById(id);
		if(polymerizeCategory==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(polymerizeCategory);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param polymerizeCategory
    */
    @RequiresPermissions("polymerize:polymerize_category:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, PolymerizeCategory polymerizeCategory) {
		return super.exportXls(request, polymerizeCategory, PolymerizeCategory.class, "总分类树");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("polymerize:polymerize_category:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, PolymerizeCategory.class);
    }

}
