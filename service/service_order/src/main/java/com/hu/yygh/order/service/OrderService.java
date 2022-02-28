package com.hu.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hu.yygh.model.order.OrderInfo;

/**
 * @author suhu
 * @createDate 2022/2/28
 */
public interface OrderService extends IService<OrderInfo> {
    long saveOrder(String scheduleId, String patientId);
}
