package com.hu.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hu.yygh.hosp.mapper.HospitalSetMapper;
import com.hu.yygh.hosp.service.HospitalSetService;
import com.hu.yygh.model.hosp.HospitalSet;
import org.springframework.stereotype.Service;

/**
 * @author suhu
 * @createDate 2022/2/7
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {


    @Override
    public HospitalSet getByHoscode(String hoscode) {
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode", hoscode);
        return baseMapper.selectOne(queryWrapper);
    }
}
