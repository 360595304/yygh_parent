package com.hu.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hu.yygh.model.hosp.HospitalSet;

public interface HospitalSetService extends IService<HospitalSet> {
    HospitalSet getByHoscode(String hoscode);
}
