package com.hu.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hu.yygh.model.order.OrderInfo;
import com.hu.yygh.vo.order.OrderQueryVo;

/**
 * @author suhu
 * @createDate 2022/2/28
 */
public interface OrderService extends IService<OrderInfo> {
    long saveOrder(String scheduleId, String patientId);

    OrderInfo getOrder(String orderId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);
}
