package com.hu.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hu.yygh.common.exception.YyghException;
import com.hu.yygh.common.helper.JwtHelper;
import com.hu.yygh.common.result.ResultCodeEnum;
import com.hu.yygh.enums.AuthStatusEnum;
import com.hu.yygh.model.user.Patient;
import com.hu.yygh.model.user.UserInfo;
import com.hu.yygh.user.mapper.UserInfoMapper;
import com.hu.yygh.user.service.PatientService;
import com.hu.yygh.user.service.UserInfoService;
import com.hu.yygh.vo.user.LoginVo;
import com.hu.yygh.vo.user.UserAuthVo;
import com.hu.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        // 检验参数
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 校验验证码
        String redisCode = redisTemplate.opsForValue().get(phone);
        if (!Objects.equals(redisCode, code)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }

        //绑定手机号码
        UserInfo userInfo = null;
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.getByOpenid(loginVo.getOpenid());
            if (null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }

        // userInfo为空， 进行正常手机登陆
        if (userInfo == null) {
            // 查询手机号是否已经注册
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", phone);
            userInfo = baseMapper.selectOne(queryWrapper);
            if (userInfo == null) { // 未注册
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setName("");
                userInfo.setStatus(1);
                this.save(userInfo);
            }
        }

        // 检验账号是否被禁用
        if (userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

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
        // token的生成
        String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
        map.put("token", token);

        return map;
    }

    @Override
    public UserInfo getByOpenid(String openId) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openId);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        UserInfo userInfo = baseMapper.selectById(userId);
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        baseMapper.updateById(userInfo);
    }

    @Override
    public void approval(Long userId, Integer status) {
        if (status == 2 ||status == -1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> show(Long userId) {
        Map<String, Object> map = new HashMap<>();
        UserInfo userInfo = baseMapper.selectById(userId);
        this.packUserInfo(userInfo);
        map.put("userInfo", userInfo);
        List<Patient> patientList = patientService.findAllByUserId(userId);
        map.put("patientList", patientList);
        return map;
    }

    @Override
    public void lock(Long id, Integer status) {
        if (status == 0 || status == 1) {
            UserInfo userInfo = baseMapper.selectById(id);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        String name = userInfoQueryVo.getKeyword();
        Integer status = userInfoQueryVo.getStatus();
        Integer authStatus = userInfoQueryVo.getAuthStatus();
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            queryWrapper.like("name", name);
        }
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(authStatus)) {
            queryWrapper.eq("auth_status", authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            queryWrapper.le("create_time", createTimeEnd);
        }
        IPage<UserInfo> pages = baseMapper.selectPage(pageParam, queryWrapper);
        pages.getRecords().forEach(this::packUserInfo);
        return pages;
    }

    private void packUserInfo(UserInfo userInfo) {
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        String statusString = userInfo.getStatus() == 0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString", statusString);
    }
}
