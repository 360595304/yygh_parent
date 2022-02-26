package com.hu.yygh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hu.yygh.common.result.Result;
import com.hu.yygh.model.user.UserInfo;
import com.hu.yygh.user.service.UserInfoService;
import com.hu.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/26
 */
@RestController
@RequestMapping("/admin/user")
@Api(tags = "后台用户管理接口")
public class UserController {
    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("用户列表(条件查询带分页)")
    @GetMapping("/{page}/{limit}")
    public Result<IPage<UserInfo>> list(@PathVariable Integer page,
                                        @PathVariable Integer limit,
                                        UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> pageParam = new Page<>(page, limit);
        IPage<UserInfo> userInfoIPage = userInfoService.selectPage(pageParam, userInfoQueryVo);
        return Result.ok(userInfoIPage);
    }

    @ApiOperation("锁定、解锁用户")
    @GetMapping("/lock/{id}/{status}")
    public Result<Object> lock(@PathVariable Long id,
                               @PathVariable Integer status) {
        userInfoService.lock(id, status);
        return Result.ok();
    }

    @ApiOperation("用户详情")
    @GetMapping("/show/{userId}")
    public Result<Map<String, Object>> show(@PathVariable Long userId) {
        Map<String, Object> map = userInfoService.show(userId);
        return Result.ok(map);
    }

    @ApiOperation("认证审批")
    @GetMapping("approval/{userId}/{status}")
    public Result<Object> approval(@PathVariable Long userId,
                                   @PathVariable Integer status) {
        userInfoService.approval(userId, status);
        return Result.ok();
    }
}
