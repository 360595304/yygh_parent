package com.hu.yygh.hosp.controller;

import com.hu.yygh.common.result.Result;
import com.hu.yygh.hosp.service.ScheduleService;
import com.hu.yygh.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author suhu
 * @createDate 2022/2/15
 */
@RestController
@RequestMapping("/admin/hosp/schedule")
@CrossOrigin
@Api(tags = "医院排班管理")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation("查询排班规则")
    @GetMapping("/getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result<Map<String, Object>> getScheduleRule(@PathVariable Integer page,
                                  @PathVariable Integer limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode) {
        Map<String, Object> map = scheduleService.getRuleSchedule(page, limit, hoscode, depcode);
        return Result.ok(map);
    }

    @ApiOperation("根据医院编号、科室编号和工作时间查询排班详细信息")
    @GetMapping("/getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result<List<Schedule>> getScheduleDetail(@PathVariable String hoscode,
                                    @PathVariable String depcode,
                                    @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getDetailSchedule(hoscode, depcode, workDate);
        return Result.ok(list);
    }
}
