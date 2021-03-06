package com.hu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hu.yygh.common.exception.YyghException;
import com.hu.yygh.common.helper.HttpRequestHelper;
import com.hu.yygh.common.rabbit.constant.MqConst;
import com.hu.yygh.common.rabbit.service.RabbitService;
import com.hu.yygh.common.result.ResultCodeEnum;
import com.hu.yygh.enums.OrderStatusEnum;
import com.hu.yygh.hosp.client.HospitalFeignClient;
import com.hu.yygh.model.order.OrderInfo;
import com.hu.yygh.model.user.Patient;
import com.hu.yygh.order.mapper.OrderMapper;
import com.hu.yygh.order.service.OrderService;
import com.hu.yygh.user.client.PatientFeignClient;
import com.hu.yygh.vo.hosp.ScheduleOrderVo;
import com.hu.yygh.vo.msm.MsmVo;
import com.hu.yygh.vo.order.OrderMqVo;
import com.hu.yygh.vo.order.OrderQueryVo;
import com.hu.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author suhu
 * @createDate 2022/2/28
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public long saveOrder(String scheduleId, String patientId) {
        // 获取排班信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        // 获取挂号人信息
        Patient patient = patientFeignClient.getPatient(patientId);
        // 判断挂号时间是否已过
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }

        // 获取签名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());
        OrderInfo orderInfo = new OrderInfo();

        BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
        String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(Long.valueOf(patientId));
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(orderInfo);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl()+"/order/submitOrder");
        System.out.println(result.toJSONString());
        if(result.getInteger("code") == 200) {
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);
            //排班可预约数
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //发送mq信息更新号源和短信通知
            // 发送mq号源更新
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(reservedNumber);
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.setTemplateCode("908e94ccf08b4476ba6c876d13f084ad");
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        } else {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }

    @Override
    public OrderInfo getOrder(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        if (orderInfo != null) this.packOrderInfo(orderInfo);
        return orderInfo;
    }

    @Override
    public OrderInfo getOrderByTradeNo(String tradeNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", tradeNo);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        if (orderInfo != null) this.packOrderInfo(orderInfo);
        return orderInfo;
    }

    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        Long userId = orderQueryVo.getUserId();
        String keyword = orderQueryVo.getKeyword();
        String orderStatus = orderQueryVo.getOrderStatus();
        Long patientId = orderQueryVo.getPatientId();
        String reserveDate = orderQueryVo.getReserveDate();
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(userId)) {
            queryWrapper.eq("user_id", userId);
        }
        if (!StringUtils.isEmpty(keyword)) {
            queryWrapper.eq("hosname", keyword);
        }
        if (!StringUtils.isEmpty(orderStatus)) {
            queryWrapper.eq("order_status", orderStatus);
        }
        if (!StringUtils.isEmpty(patientId)) {
            queryWrapper.eq("patient_id", patientId);
        }
        if (!StringUtils.isEmpty(reserveDate)) {
            queryWrapper.eq("reserve_date", reserveDate);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            queryWrapper.le("create_time", createTimeEnd);
        }
        Page<OrderInfo> orderInfoPage = baseMapper.selectPage(pageParam, queryWrapper);
        orderInfoPage.getRecords().forEach(this::packOrderInfo);
        return orderInfoPage;
    }

    private void packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
    }

    @Override
    public void setStatus(String tradeNo, int status) {
        OrderInfo orderInfo = this.getOrderByTradeNo(tradeNo);
        orderInfo.setOrderStatus(status);
        this.updateById(orderInfo);
    }
}
