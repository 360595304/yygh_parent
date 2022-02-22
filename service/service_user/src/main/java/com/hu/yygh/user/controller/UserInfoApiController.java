package com.hu.yygh.user.controller;

import com.hu.yygh.common.result.Result;
import com.hu.yygh.user.service.UserInfoService;
import com.hu.yygh.vo.user.LoginVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/21
 */
@RequestMapping("/api/user")
@RestController
@Api(tags = "用户管理接口")
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("/login")
    @ApiOperation("会员登陆")
    public Result<Map<String, Object>> login(@RequestBody LoginVo loginVo,
                                             HttpServletRequest request) {
        Map<String, Object> map = userInfoService.login(loginVo);
        return Result.ok(map);
    }
}
