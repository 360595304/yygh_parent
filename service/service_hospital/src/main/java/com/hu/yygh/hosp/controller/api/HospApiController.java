package com.hu.yygh.hosp.controller.api;

import com.hu.yygh.common.result.Result;
import com.hu.yygh.hosp.service.DepartmentService;
import com.hu.yygh.hosp.service.HospitalService;
import com.hu.yygh.model.hosp.Hospital;
import com.hu.yygh.vo.hosp.DepartmentVo;
import com.hu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/16
 */
@RestController
@RequestMapping("/api/hosp/hospital")
@Api(tags = "前端请求接口")
public class HospApiController {
    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation("条件分页获取医院列表")
    @GetMapping("/findHospList/{page}/{limit}")
    public Result<Page<Hospital>> findHospList(@PathVariable Integer page,
                               @PathVariable Integer limit,
                               HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitalPage = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitalPage);
    }

    @ApiOperation("根据医院名称查询")
    @GetMapping("/findByHosname/{hosname}")
    public Result<List<Hospital>> findByHosname(@PathVariable String hosname) {
        List<Hospital> hospitalList = hospitalService.findByHosname(hosname);
        return Result.ok(hospitalList);
    }

    @ApiOperation("根据医院编号获取科室信息")
    @GetMapping("/department/{hoscode}")
    public Result<List<DepartmentVo>> index(@PathVariable String hoscode) {
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return Result.ok(list);
    }

    @ApiOperation("根据医院编号获取预约挂号详情")
    @GetMapping("/{hoscode}")
    public Result<Map<String, Object>> item(@PathVariable String hoscode) {
        Map<String, Object> map = hospitalService.item(hoscode);
        return Result.ok(map);
    }
}
