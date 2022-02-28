package com.hu.yygh.hosp.service;

import com.hu.yygh.model.hosp.Schedule;
import com.hu.yygh.vo.hosp.ScheduleOrderVo;
import com.hu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void save(Map<String, Object> paramMap);

    Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getRuleSchedule(Integer page, Integer limit, String hoscode, String depcode);

    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule getScheduleById(String scheduleId);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);
}
