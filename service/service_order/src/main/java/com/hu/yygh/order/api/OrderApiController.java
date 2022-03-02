package com.hu.yygh.order.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hu.yygh.common.result.Result;
import com.hu.yygh.common.utils.AuthContextHolder;
import com.hu.yygh.enums.OrderStatusEnum;
import com.hu.yygh.model.order.OrderInfo;
import com.hu.yygh.order.service.OrderService;
import com.hu.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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
    @PostMapping("/auth/submitOrder/{scheduleId}/{patientId}")
    public Result<Long> saveOrders(@PathVariable String scheduleId,
                                     @PathVariable String patientId) {
        Long orderId = orderService.saveOrder(scheduleId, patientId);
        return Result.ok(orderId);
    }

    @GetMapping("/auth/getOrder/{orderId}")
    @ApiOperation("根据id获取订单信息")
    public Result<OrderInfo> getOrder(@PathVariable String orderId) {
        OrderInfo orderInfo = orderService.getOrder(orderId);
        return Result.ok(orderInfo);
    }

    @GetMapping("/auth/{page}/{limit}")
    @ApiOperation("用户订单列表")
    public Result<IPage<OrderInfo>> list(@PathVariable Integer page,
                              @PathVariable Integer limit,
                              OrderQueryVo orderQueryVo,
                              HttpServletRequest request) {
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(pageParam, orderQueryVo);
        return Result.ok(pageModel);
    }

    @GetMapping("/auth/getStatusList")
    @ApiOperation("获取订单的所有状态")
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }


}
