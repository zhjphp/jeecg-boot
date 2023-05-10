package org.jeecg.modules.polymerize.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.dto.InformationSourceComponentDTO;
import org.jeecg.modules.polymerize.dto.InformationSourceDTO;
import org.jeecg.modules.polymerize.entity.InformationSource;
import org.jeecg.modules.polymerize.service.IInformationSourceService;
import org.jeecg.modules.polymerize.vo.InformationSourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
* @Description: 信源管理
* @Author: jeecg-boot
* @Date:   2023-04-17
* @Version: V1.0
*/
@Api(tags="信源管理")
@RestController
@RequestMapping("/polymerize/informationSource")
@Slf4j
public class InformationSourceController extends JeecgController<InformationSource, IInformationSourceService> {
   @Autowired
   private IInformationSourceService informationSourceService;

    /**
     * 按分类分页列表查询
     *
     * @param informationSourceDTO
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
   @ApiOperation(value="信源管理-分页列表查询", notes="信源管理-分页列表查询")
   @GetMapping(value = "/list")
   public Result<IPage<InformationSourceVO>> queryPageListByCategory(InformationSourceDTO informationSourceDTO,
                                                                     @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                                     @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                                     HttpServletRequest req) {
       IPage<InformationSourceVO> pageList = informationSourceService.queryPageList(informationSourceDTO, pageNo, pageSize);
       return Result.OK(pageList);
//        String categoryId = informationSourceDTO.getCategoryId();
//        if (oConvertUtils.isNotEmpty(categoryId)) {
//            IPage<InformationSourceVO> pageList = informationSourceService.queryPageList(informationSourceDTO, pageNo, pageSize);
//            return Result.OK(pageList);
//        } else {
//            InformationSource informationSource = informationSourceDTO;
//            QueryWrapper<InformationSource> queryWrapper = QueryGenerator.initQueryWrapper(informationSource, req.getParameterMap());
//            Page<InformationSource> page = new Page<InformationSource>(pageNo, pageSize);
//            IPage<InformationSource> pageList = informationSourceService.page(page, queryWrapper);
//            return Result.OK(pageList);
//        }
   }

    /**
     * 前端组件 JSelectInformationSourceByCategory 专用，任务添加选择信源
     * @param informationSourceComponentDTO
     * @return
     */
    @ApiOperation(value="信源管理-分页列表查询", notes="信源管理-分页列表查询")
    @PostMapping(value = "/componentList")
    public Result<IPage<InformationSourceVO>> queryByComponentData(@RequestBody InformationSourceComponentDTO informationSourceComponentDTO) {
        int pageNo = informationSourceComponentDTO.getPageNo();
        int pageSize = informationSourceComponentDTO.getPageSize();
        IPage<InformationSourceVO> pageList = informationSourceService.queryByComponentData(informationSourceComponentDTO, pageNo, pageSize);
        return Result.OK(pageList);
    }

   /**
    * 添加
    *
    * @param informationSourceDTO
    * @return
    */
   @AutoLog(value = "信源管理-添加")
   @ApiOperation(value="信源管理-添加", notes="信源管理-添加")
//	@RequiresPermissions("polymerize:polymerize_information_source:add")
   @PostMapping(value = "/add")
   public Result<String> add(@RequestBody InformationSourceDTO informationSourceDTO) {
       try {
           informationSourceService.add(informationSourceDTO);
       } catch (Exception e) {
           return Result.error(e.getMessage());
       }
       return Result.OK("添加成功！");
   }

   /**
    * 编辑
    *
    * @param informationSourceDTO
    * @return
    */
   @AutoLog(value = "信源管理-编辑")
   @ApiOperation(value="信源管理-编辑", notes="信源管理-编辑")
//	@RequiresPermissions("polymerize:polymerize_information_source:edit")
   @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
   public Result<String> edit(@RequestBody InformationSourceDTO informationSourceDTO) {
       try {
           informationSourceService.edit(informationSourceDTO);
       } catch (Exception e) {
           return Result.error(e.getMessage());
       }
       return Result.OK("编辑成功！");
   }

   /**
    * 通过id删除
    *
    * @param id
    * @return
    */
   @AutoLog(value = "信源管理-通过id删除")
   @ApiOperation(value="信源管理-通过id删除", notes="信源管理-通过id删除")
//	@RequiresPermissions("polymerize:polymerize_information_source:delete")
   @DeleteMapping(value = "/delete")
   public Result<String> delete(@RequestParam(name="id",required=true) String id) {
       try {
           informationSourceService.delete(id);
       } catch (Exception e) {
           return Result.OK("删除失败!");
       }
       return Result.OK("删除成功!");
   }

   /**
    *  批量删除
    *
    * @param ids
    * @return
    */
   @AutoLog(value = "信源管理-批量删除")
   @ApiOperation(value="信源管理-批量删除", notes="信源管理-批量删除")
//	@RequiresPermissions("polymerize:polymerize_information_source:deleteBatch")
   @DeleteMapping(value = "/deleteBatch")
   public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
       try {
           String[] idsArr = ids.split(",");
           for (String id : idsArr) {
               informationSourceService.delete(id);
           }
       } catch (Exception e) {
           return Result.OK("删除失败!");
       }
       return Result.OK("批量删除成功!");
   }

   /**
    * 通过id查询
    *
    * @param id
    * @return
    */
   //@AutoLog(value = "信源管理-通过id查询")
   @ApiOperation(value="信源管理-通过id查询", notes="信源管理-通过id查询")
   @GetMapping(value = "/queryById")
   public Result<InformationSource> queryById(@RequestParam(name="id",required=true) String id) {
       InformationSource informationSource = informationSourceService.getById(id);
       if(informationSource==null) {
           return Result.error("未找到对应数据");
       }
       return Result.OK(informationSource);
   }

//    /**
//    * 导出excel
//    *
//    * @param request
//    * @param polymerizeInformationSource
//    */
//    @RequiresPermissions("polymerize:polymerize_information_source:exportXls")
//    @RequestMapping(value = "/exportXls")
//    public ModelAndView exportXls(HttpServletRequest request, PolymerizeInformationSource polymerizeInformationSource) {
//        return super.exportXls(request, polymerizeInformationSource, PolymerizeInformationSource.class, "信源管理");
//    }
//
//    /**
//      * 通过excel导入数据
//    *
//    * @param request
//    * @param response
//    * @return
//    */
//    @RequiresPermissions("polymerize:polymerize_information_source:importExcel")
//    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
//    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
//        return super.importExcel(request, response, PolymerizeInformationSource.class);
//    }

}
