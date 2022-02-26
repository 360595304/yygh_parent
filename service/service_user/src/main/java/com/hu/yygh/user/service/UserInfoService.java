package com.hu.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hu.yygh.model.user.UserInfo;
import com.hu.yygh.vo.user.LoginVo;
import com.hu.yygh.vo.user.UserAuthVo;
import com.hu.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/21
 */
public interface UserInfoService extends IService<UserInfo> {
    Map<String, Object> login(LoginVo loginVo);

    UserInfo getByOpenid(String openId);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    void lock(Long id, Integer status);

    Map<String, Object> show(Long userId);

    void approval(Long userId, Integer status);
}
