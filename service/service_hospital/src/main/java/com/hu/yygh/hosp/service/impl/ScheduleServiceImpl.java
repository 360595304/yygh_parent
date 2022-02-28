package com.hu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hu.yygh.common.exception.YyghException;
import com.hu.yygh.common.result.ResultCodeEnum;
import com.hu.yygh.hosp.repository.ScheduleRepository;
import com.hu.yygh.hosp.service.DepartmentService;
import com.hu.yygh.hosp.service.HospitalService;
import com.hu.yygh.hosp.service.ScheduleService;
import com.hu.yygh.model.hosp.BookingRule;
import com.hu.yygh.model.hosp.Department;
import com.hu.yygh.model.hosp.Hospital;
import com.hu.yygh.model.hosp.Schedule;
import com.hu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.hu.yygh.vo.hosp.ScheduleOrderVo;
import com.hu.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        // 构建可预约日期列表 年-月-日
        IPage<Date> iPage = this.getDateList(page, limit, bookingRule);
        List<Date> dateList = iPage.getRecords();
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")

        );
        AggregationResults<BookingScheduleRuleVo> aggregate =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleRuleVoList = aggregate.getMappedResults();
        // 根据日期转换成map
        Map<Date, BookingScheduleRuleVo> scheduleRuleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleRuleVoList)) {
            scheduleRuleVoMap = scheduleRuleVoList.stream().collect(
                    Collectors.toMap(
                            BookingScheduleRuleVo::getWorkDate, bookingScheduleRuleVo -> bookingScheduleRuleVo));
        }
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = dateList.size(); i < len; i++) {
            Date date = dateList.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleRuleVoMap.get(date);
            // 该天没有放号
            if (bookingScheduleRuleVo == null) {
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                bookingScheduleRuleVo.setAvailableNumber(-1);
                bookingScheduleRuleVo.setDocCount(0);
            }
            // 设置相关属性
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            bookingScheduleRuleVo.setDayOfWeek(this.getDayOfWeek(new DateTime(date)));

            if (i == len - 1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            if (i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) {
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);

        }
        // 封装数据
        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);

        return result;
    }

    @Override
    public Schedule getScheduleById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        this.packageSchedule(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        BookingRule bookingRule = hospital.getBookingRule();

        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        scheduleOrderVo.setStopTime(stopTime.toDate());
        return scheduleOrderVo;

    }

    private IPage<Date> getDateList(Integer page, Integer limit, BookingRule bookingRule) {
        // 获取完整的放号时间 年-月-日 时:分
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        // 预约周期
        Integer cycle = bookingRule.getCycle();
        if (releaseTime.isBeforeNow()) {
            ++cycle;
        }
        // 构建可预约日期列表
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime curDateTime = new DateTime().plusDays(i);
            String s = curDateTime.toString("yyyy-MM-dd");
            DateTime dateTime = new DateTime(s);
            dateList.add(dateTime.toDate());
        }
        // 分页预约日期
        List<Date> pageDateList = new ArrayList<>();
        int start = (page - 1) * limit;
        int end = start + limit;
        if (end > dateList.size()) end = dateList.size(); // 终止不能超过列表长度
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        // 构建分页对象
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, 7);
        iPage.setRecords(pageDateList);
        iPage.setTotal(cycle);
        iPage.setPages(cycle/limit + 1);
        return iPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
    }


    private void packageSchedule(Schedule schedule) {
        Map<String, Object> param = schedule.getParam();
        param.put("hosname", hospitalService.getHospName(schedule.getHoscode()));
        param.put("depname", departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        param.put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }
}
