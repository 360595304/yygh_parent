package com.hu.yygh.order.api;

import com.hu.yygh.common.result.Result;
import com.hu.yygh.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author suhu
 * @createDate 2022/2/28
 */
@RestController
@RequestMapping("/api/order/orderInfo")
@Api(tags = "订单接口")
public class OrderApiController {
    @Autowired
    private OrderService orderService;

    @ApiOperation("生成挂号订单")
    @GetMapping("/auth/submitOrder/{scheduleId}/{patientId}")
    public Result<Long> saveOrders(@PathVariable String scheduleId,
                                     @PathVariable String patientId) {
        Long orderId = orderService.saveOrder(scheduleId, patientId);
        return Result.ok(orderId);
    }

}
