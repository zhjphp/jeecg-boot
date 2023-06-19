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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.polymerize.drawflow.model.ArticleRuleNode;
import org.jeecg.modules.polymerize.drawflow.model.ListRuleNode;
import org.jeecg.modules.polymerize.entity.InformationSourceRule;
import org.jeecg.modules.polymerize.service.IInformationSourceRuleService;

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
 * @Description: 信源规则
 * @Author: jeecg-boot
 * @Date:   2023-04-29
 * @Version: V1.0
 */
@Api(tags="信源规则")
@RestController
@RequestMapping("/polymerize/informationSourceRule")
@Slf4j
public class InformationSourceRuleController extends JeecgController<InformationSourceRule, IInformationSourceRuleService> {
    @Autowired
    private IInformationSourceRuleService informationSourceRuleService;

    @ApiOperation(value="信源规则-测试列表规则", notes="测试列表规则-checkListRule")
    @PostMapping("/checkListRule")
    public Result<JSONObject> checkListRule(@RequestBody String jsonRequest) {
        log.info("请求内容:, {}", jsonRequest);
        ListRuleNode listRuleNode = new ListRuleNode(JSON.parseObject(jsonRequest));
        try {
            JSONObject result = informationSourceRuleService.checkListRule(listRuleNode);
            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation(value="信源规则-测试详情规则", notes="测试详情规则-checkArticleRule")
    @PostMapping("/checkArticleRule")
    public Result<JSONObject> checkArticleRule(@RequestBody String jsonRequest) {
        log.info("请求内容:, {}", jsonRequest);
        ArticleRuleNode articleRuleNode = new ArticleRuleNode(JSON.parseObject(jsonRequest));
        try {
            JSONObject result = informationSourceRuleService.checkArticleRule(articleRuleNode);
            return Result.ok(result);
        } catch (Exception e) {
            log.info("测试详情规则错误: {}, {}", e.getMessage(), e.toString());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 配置规则
     *
     * @param jsonRequest
     * @return
     */
    @ApiOperation(value="信源规则-配置规则", notes="信源规则-configureRule")
    @PostMapping("/configureRule")
    public Result<String> configureRule(@RequestBody String jsonRequest) {
        log.info(jsonRequest);
        // 过滤数据中的换行符
        String jsonString = jsonRequest.replace("\\n", "");
        JSONObject jSONObject = JSON.parseObject(jsonString);
        // JSONObject jSONObject = JSON.parseObject(jsonRequest);
        // 信源ID
        String informationSourceId = jSONObject.getString("informationSourceId");
        log.info("信源ID informationSourceId: " + informationSourceId);
        // 规则配置
        String drawflowConfig = jSONObject.getString("drawflowConfig");
        log.info("规则配置 drawflowConfig: " + drawflowConfig);
        try {
            informationSourceRuleService.configureRule(informationSourceId, drawflowConfig);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.ok("配置成功");
    }

    /**
     * 按信源ID查询规则
     *
     * @param informationSourceId
     * @return
     */
    //@AutoLog(value = "信源规则-按信源ID查询规则")
    @ApiOperation(value="信源规则-按信源ID查询规则", notes="信源规则-按信源ID查询规则")
    @GetMapping(value = "/getRule")
    public Result<InformationSourceRule> queryPageList(@RequestParam(name="informationSourceId") String informationSourceId) {
        if (oConvertUtils.isEmpty(informationSourceId)) {
            return Result.error("信源ID为空");
        }
        InformationSourceRule result = informationSourceRuleService.getOne(new LambdaQueryWrapper<InformationSourceRule>().eq(InformationSourceRule::getInformationSourceId, informationSourceId));
        return Result.OK(result);
    }

//    /**
//     * 分页列表查询
//     *
//     * @param informationSourceRule
//     * @param pageNo
//     * @param pageSize
//     * @param req
//     * @return
//     */
//    //@AutoLog(value = "信源规则-分页列表查询")
//    @ApiOperation(value="信源规则-分页列表查询", notes="信源规则-分页列表查询")
//    @GetMapping(value = "/list")
//    public Result<IPage<InformationSourceRule>> queryPageList(InformationSourceRule informationSourceRule,
//                                                              @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
//                                                              @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
//                                                              HttpServletRequest req) {
//        QueryWrapper<InformationSourceRule> queryWrapper = QueryGenerator.initQueryWrapper(informationSourceRule, req.getParameterMap());
//        Page<InformationSourceRule> page = new Page<InformationSourceRule>(pageNo, pageSize);
//        IPage<InformationSourceRule> pageList = informationSourceRuleService.page(page, queryWrapper);
//        return Result.OK(pageList);
//    }
//
//    /**
//     *   添加
//     *
//     * @param informationSourceRule
//     * @return
//     */
//    @AutoLog(value = "信源规则-添加")
//    @ApiOperation(value="信源规则-添加", notes="信源规则-添加")
//    @RequiresPermissions("polymerize:information_source_rule:add")
//    @PostMapping(value = "/add")
//    public Result<String> add(@RequestBody InformationSourceRule informationSourceRule) {
//        informationSourceRuleService.save(informationSourceRule);
//        return Result.OK("添加成功！");
//    }
//
//    /**
//     *  编辑
//     *
//     * @param informationSourceRule
//     * @return
//     */
//    @AutoLog(value = "信源规则-编辑")
//    @ApiOperation(value="信源规则-编辑", notes="信源规则-编辑")
//    @RequiresPermissions("polymerize:information_source_rule:edit")
//    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
//    public Result<String> edit(@RequestBody InformationSourceRule informationSourceRule) {
//        informationSourceRuleService.updateById(informationSourceRule);
//        return Result.OK("编辑成功!");
//    }
//
//    /**
//     *   通过id删除
//     *
//     * @param id
//     * @return
//     */
//    @AutoLog(value = "信源规则-通过id删除")
//    @ApiOperation(value="信源规则-通过id删除", notes="信源规则-通过id删除")
//    @RequiresPermissions("polymerize:information_source_rule:delete")
//    @DeleteMapping(value = "/delete")
//    public Result<String> delete(@RequestParam(name="id",required=true) String id) {
//        informationSourceRuleService.removeById(id);
//        return Result.OK("删除成功!");
//    }
//
//    /**
//     *  批量删除
//     *
//     * @param ids
//     * @return
//     */
//    @AutoLog(value = "信源规则-批量删除")
//    @ApiOperation(value="信源规则-批量删除", notes="信源规则-批量删除")
//    @RequiresPermissions("polymerize:information_source_rule:deleteBatch")
//    @DeleteMapping(value = "/deleteBatch")
//    public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
//        this.informationSourceRuleService.removeByIds(Arrays.asList(ids.split(",")));
//        return Result.OK("批量删除成功!");
//    }
//
//    /**
//     * 通过id查询
//     *
//     * @param id
//     * @return
//     */
//    //@AutoLog(value = "信源规则-通过id查询")
//    @ApiOperation(value="信源规则-通过id查询", notes="信源规则-通过id查询")
//    @GetMapping(value = "/queryById")
//    public Result<InformationSourceRule> queryById(@RequestParam(name="id",required=true) String id) {
//        InformationSourceRule informationSourceRule = informationSourceRuleService.getById(id);
//        if(informationSourceRule==null) {
//            return Result.error("未找到对应数据");
//        }
//        return Result.OK(informationSourceRule);
//    }
//
//    /**
//     * 导出excel
//     *
//     * @param request
//     * @param informationSourceRule
//     */
//    @RequiresPermissions("polymerize:information_source_rule:exportXls")
//    @RequestMapping(value = "/exportXls")
//    public ModelAndView exportXls(HttpServletRequest request, InformationSourceRule informationSourceRule) {
//        return super.exportXls(request, informationSourceRule, InformationSourceRule.class, "信源规则");
//    }
//
//    /**
//     * 通过excel导入数据
//     *
//     * @param request
//     * @param response
//     * @return
//     */
//    @RequiresPermissions("polymerize:information_source_rule:importExcel")
//    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
//    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
//        return super.importExcel(request, response, InformationSourceRule.class);
//    }

}
