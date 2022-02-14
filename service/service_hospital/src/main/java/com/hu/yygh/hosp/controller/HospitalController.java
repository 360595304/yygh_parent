package com.hu.yygh.hosp.controller;

import com.hu.yygh.common.result.Result;
import com.hu.yygh.hosp.service.HospitalService;
import com.hu.yygh.model.hosp.Hospital;
import com.hu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/14
 */
@RestController
@RequestMapping("/admin/hosp/hospital")
@CrossOrigin
@Api(tags = "医院管理")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @ApiOperation("医院列表")
    @GetMapping("/list/{page}/{limit}")
    public Result<Page<Hospital>> hospList(@PathVariable int page,
                           @PathVariable int limit,
                           HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitalPage = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitalPage);
    }

    @ApiOperation("更新医院上线状态")
    @GetMapping("/updateHospitalStatus/{id}/{status}")
    public Result<Object> updateHospitalStatus(@PathVariable String id,
                                               @PathVariable Integer status) {
        hospitalService.updateStatus(id, status);
        return Result.ok();
    }

    @ApiOperation("获取医院详情信息")
    @GetMapping("/showHospitalDetail/{id}")
    public Result<Map<String, Object>> showHospitalDetail(@PathVariable String id) {
        Map<String, Object> map = hospitalService.getHospById(id);
        return Result.ok(map);
    }
}
