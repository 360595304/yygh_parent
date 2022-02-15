package com.hu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hu.yygh.hosp.repository.ScheduleRepository;
import com.hu.yygh.hosp.service.DepartmentService;
import com.hu.yygh.hosp.service.HospitalService;
import com.hu.yygh.hosp.service.ScheduleService;
import com.hu.yygh.model.hosp.Schedule;
import com.hu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.hu.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/13
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> paramMap) {
        String jsonString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);
        Schedule scheduleExist = scheduleRepository
                .getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        if (scheduleExist != null) {
            schedule.setId(scheduleExist.getId());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        } else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);
        Example<Schedule> example = Example.of(schedule, matcher);
        return scheduleRepository.findAll(example, pageable);
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule != null) {
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    @Override
    public Map<String, Object> getRuleSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        // 根据 医院编号 和 科室编号 查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        // 根据工作日期workDate进行分组
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        // 统计号源数量
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate"),
                // 分页
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        // 查询
        AggregationResults<BookingScheduleRuleVo> aggResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResults.getMappedResults();
        // 总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        int total = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class).getMappedResults().size();
        for (BookingScheduleRuleVo scheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = scheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            scheduleRuleVo.setDayOfWeek(dayOfWeek);
            scheduleRuleVo.setWorkDateMd(scheduleRuleVo.getWorkDate());
        }
        Map<String, Object> result = new HashMap<>();
        String hospName = hospitalService.getHospName(hoscode);
        // 信息封装到map
        result.put("bookingScheduleRuleVoList", bookingScheduleRuleVoList);
        result.put("total", total);
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hospName);
        result.put("baseMap", baseMap);
        return result;
    }

    /**
     * 根据日期获取周几数据
     *
     * @param dateTime 日期
     * @return 星期几
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        scheduleList.forEach(this::packageSchedule);
        return scheduleList;
    }

    private void packageSchedule(Schedule schedule) {
        Map<String, Object> param = schedule.getParam();
        param.put("hosname", hospitalService.getHospName(schedule.getHoscode()));
        param.put("depname", departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        param.put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }
}
