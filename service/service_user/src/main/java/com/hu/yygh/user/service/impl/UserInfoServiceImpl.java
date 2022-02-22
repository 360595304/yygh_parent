package com.hu.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hu.yygh.common.exception.YyghException;
import com.hu.yygh.common.helper.JwtHelper;
import com.hu.yygh.common.result.ResultCodeEnum;
import com.hu.yygh.model.user.UserInfo;
import com.hu.yygh.user.mapper.UserInfoMapper;
import com.hu.yygh.user.service.UserInfoService;
import com.hu.yygh.vo.user.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author suhu
 * @createDate 2022/2/21
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        // 检验参数
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        if (StringUtils.isEmpty(phone) ||StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 校验验证码
        String redisCode = redisTemplate.opsForValue().get(phone);
        if (!Objects.equals(redisCode, code)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        // 查询手机号是否已经注册
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        if (userInfo == null) { // 未注册
            userInfo = new UserInfo();
            userInfo.setPhone(phone);
            userInfo.setName("");
            userInfo.setStatus(1);
            this.save(userInfo);
        }

        // 检验账号是否被禁用
        if (userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        // todo 记录登陆

        // 返回页面返回名称
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        // todo token的生成
        String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
        map.put("token", token);

        return map;
    }
}
