package com.hu.yygh.hosp.controller;

import com.hu.yygh.common.result.Result;
import com.hu.yygh.hosp.service.DepartmentService;
import com.hu.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author suhu
 * @createDate 2022/2/15
 */
@RestController
@RequestMapping("/admin/hosp/department")
@Api(tags = "医院科室管理")
@CrossOrigin
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @ApiOperation("根据医院id查询医院的所有科室")
    @GetMapping("/getDeptList/{hoscode}")
    public Result<List<DepartmentVo>> getDeptList(@PathVariable String hoscode) {
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return Result.ok(list);
    }
}
