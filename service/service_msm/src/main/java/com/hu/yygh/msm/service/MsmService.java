package com.hu.yygh.msm.service;

import com.hu.yygh.vo.msm.MsmVo;

public interface MsmService {
    // 发送验证码
    boolean send(String phone, String code);

    // MQ通知发送短信
    boolean send(MsmVo msmVo);
}
