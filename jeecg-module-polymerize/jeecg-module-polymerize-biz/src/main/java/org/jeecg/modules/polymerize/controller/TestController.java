package org.jeecg.modules.polymerize.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

@Slf4j
@Api(tags="Polymerize测试")
@RestController
// @Controller
@RequestMapping("/polymerize/test")
public class TestController {

    @Resource
    ISysBaseAPI sysBaseAPI;

    @ApiOperation(value="feignSystemBaseAPI", notes="feignSystemBaseAPI")
    @GetMapping("/feign_system")
    public Result<String> feignSystemBaseAPI(HttpServletRequest request,HttpServletResponse response) {
        String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
        String username = JwtUtil.getUsername(token);
        LoginUser sysUser = sysBaseAPI.getUserByName(username);

        List<String> role = sysBaseAPI.getRolesByUsername(sysUser.getUsername());
        log.info("role----------------");
        log.info(role.toString());

        Set<String> permission = sysBaseAPI.getUserPermissionSet(sysUser.getUsername());
        log.info("permission----------------");
        log.info(permission.toString());

        return Result.ok("TestController-feignSystemBaseAPI");
    }

    @ApiOperation(value="返回string-测试", notes="返回string-测试")
    @GetMapping("/response_string")
    public Result<String> responseString() {
        return Result.ok("TestController-responseString");
    }

    @ApiOperation(value="auth_permission-测试", notes="auth_permission-测试")
    // @PostMapping ("/auth_permission")
    @RequestMapping(value = "/auth_permission", method = {RequestMethod.POST})
    @RequiresPermissions("polymerize:auth:test")
    // @ResponseBody
    public Result<String> authPermission() {
        return Result.ok("TestController-authPermission");
    }

}
