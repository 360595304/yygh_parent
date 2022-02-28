package com.hu.yygh.user.api;

import com.hu.yygh.common.result.Result;
import com.hu.yygh.common.utils.AuthContextHolder;
import com.hu.yygh.model.user.Patient;
import com.hu.yygh.user.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author suhu
 * @createDate 2022/2/26
 */
@RequestMapping("/api/user/patient")
@RestController
@Api(tags = "就诊人管理")
public class PatientApiController {
    @Autowired
    private PatientService patientService;

    @ApiOperation("获取就诊人列表")
    @GetMapping("/auth/findAll")
    public Result<Object> findAll(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> patientList = patientService.findAllByUserId(userId);
        return Result.ok(patientList);
    }

    @ApiOperation("添加就诊人")
    @PostMapping("/auth/save")
    public Result<Object> save(@RequestBody Patient patient, HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }

    @ApiOperation("根据id获取就诊人信息")
    @GetMapping("/auth/get/{id}")
    public Result<Patient> get(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }

    @ApiOperation("修改就诊人")
    @PostMapping("/auth/update")
    public Result<Object> updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return Result.ok();
    }

    @ApiOperation("删除就诊人")
    @DeleteMapping("auth/remove/{id}")
    public Result<Object> removePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return Result.ok();
    }

    @ApiOperation("根据就诊人id获取就诊人信息")
    @GetMapping("/inner/{id}")
    public Patient getPatient(@PathVariable Long id) {
        return patientService.getPatientById(id);
    }
}
