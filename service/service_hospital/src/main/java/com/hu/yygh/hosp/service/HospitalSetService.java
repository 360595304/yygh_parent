package com.hu.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hu.yygh.model.hosp.HospitalSet;
import com.hu.yygh.vo.order.SignInfoVo;

public interface HospitalSetService extends IService<HospitalSet> {
    HospitalSet getByHoscode(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
