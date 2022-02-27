package com.hu.yygh.hosp.controller.api;

import com.hu.yygh.common.exception.YyghException;
import com.hu.yygh.common.helper.HttpRequestHelper;
import com.hu.yygh.common.result.Result;
import com.hu.yygh.common.result.ResultCodeEnum;
import com.hu.yygh.common.utils.MD5;
import com.hu.yygh.hosp.service.DepartmentService;
import com.hu.yygh.hosp.service.HospitalService;
import com.hu.yygh.hosp.service.HospitalSetService;
import com.hu.yygh.hosp.service.ScheduleService;
import com.hu.yygh.model.hosp.Department;
import com.hu.yygh.model.hosp.Hospital;
import com.hu.yygh.model.hosp.HospitalSet;
import com.hu.yygh.model.hosp.Schedule;
import com.hu.yygh.vo.hosp.DepartmentQueryVo;
import com.hu.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/13
 */
@RestController
@RequestMapping("/api/hosp")
@Api("医院端接口")
public class ApiController {

    // mongoDB
    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    // MySQL
    @Autowired
    private HospitalSetService hospitalSetService;


    @ApiOperation("添加/更新医院")
    @PostMapping("/saveHospital")
    public Result<Object> saveHospital(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        if (diff(paramMap.get("hoscode"), paramMap.get("sign")))
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        String logoData = (String) paramMap.get("logoData");
        logoData = logoData.replace(" ", "+");
        paramMap.put("logoData", logoData);
        hospitalService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation("获取医院信息")
    @PostMapping("/hospital/show")
    public Result<Hospital> getHospital(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        String hoscode = (String) paramMap.get("hoscode");
        if (diff(paramMap.get("hoscode"), paramMap.get("sign")))
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    @ApiOperation("新增科室信息")
    @PostMapping("/saveDepartment")
    public Result<Object> saveDepartment(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        if (diff(paramMap.get("hoscode"), paramMap.get("sign")))
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);

        departmentService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation("获取科室信息")
    @PostMapping("/department/list")
    public Result<Page<Department>> getDepartment(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        if (diff(paramMap.get("hoscode"), paramMap.get("sign")))
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        String hoscode = (String) paramMap.get("hoscode");
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        Page<Department> departmentPage = departmentService.findPageDepartment(page, limit, departmentQueryVo);
        return Result.ok(departmentPage);
    }

    @ApiOperation("删除科室")
    @PostMapping("/department/remove")
    public Result<Object> removeDepartment(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        if (diff(paramMap.get("hoscode"), paramMap.get("sign")))
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");

        departmentService.remove(hoscode, depcode);
        return Result.ok();
    }

    @ApiOperation("添加排班")
    @PostMapping("/saveSchedule")
    public Result<Object> saveSchedule(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        if (diff(paramMap.get("hoscode"), paramMap.get("sign")))
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);

        scheduleService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation("排班查询")
    @PostMapping("/schedule/list")
    public Result<Page<Schedule>> findPageSchedule(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        if (diff(paramMap.get("hoscode"), paramMap.get("sign")))
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        Page<Schedule> schedulePage = scheduleService.findPageSchedule(page, limit, scheduleQueryVo);
        return Result.ok(schedulePage);
    }

    @ApiOperation("删除排班")
    @PostMapping("/schedule/remove")
    public Result<Object> removeSchedule(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        if (diff(paramMap.get("hoscode"), paramMap.get("sign")))
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");
        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();
    }



    private boolean diff(Object hoscode, Object hospSign) {
        HospitalSet hospitalSet = hospitalSetService.getByHoscode((String) hoscode);
        String hospSignMD5 = MD5.encrypt(hospitalSet.getSignKey());
        return !hospSign.equals(hospSignMD5);
    }
}
