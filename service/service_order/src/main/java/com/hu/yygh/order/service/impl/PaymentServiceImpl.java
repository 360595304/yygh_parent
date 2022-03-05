package com.hu.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hu.yygh.enums.PaymentStatusEnum;
import com.hu.yygh.model.order.OrderInfo;
import com.hu.yygh.model.order.PaymentInfo;
import com.hu.yygh.order.mapper.PaymentMapper;
import com.hu.yygh.order.service.PaymentService;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author suhu
 * @createDate 2022/3/3
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderInfo.getId());
        queryWrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(queryWrapper);
        if (count > 0) return;
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + "|" + orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);
    }
}
