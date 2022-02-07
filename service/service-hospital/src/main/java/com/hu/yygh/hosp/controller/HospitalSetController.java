package com.hu.yygh.hosp.controller;

import com.hu.yygh.hosp.service.HospitalSetService;
import com.hu.yygh.model.hosp.HospitalSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author suhu
 * @createDate 2022/2/7
 */
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@Api(tags = "医院设置管理")
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("获取分页列表")
    @GetMapping("/findAll")
    public List<HospitalSet> findAllHospitalSet() {
        return hospitalSetService.list();
    }

    @ApiOperation("逻辑删除医院信息")
    @DeleteMapping("/{id}")
    public boolean removeHospitalSet(@PathVariable Long id) {
        return hospitalSetService.removeById(id);
    }
}
