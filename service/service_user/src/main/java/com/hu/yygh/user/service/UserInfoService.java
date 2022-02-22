package com.hu.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hu.yygh.model.user.UserInfo;
import com.hu.yygh.vo.user.LoginVo;

import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/21
 */
public interface UserInfoService extends IService<UserInfo> {
    Map<String, Object> login(LoginVo loginVo);
}
