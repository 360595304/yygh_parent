package com.hu.yygh.order.api;

import com.hu.yygh.common.result.Result;
import com.hu.yygh.order.service.AlipayService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * @author suhu
 * @createDate 2022/3/3
 */
@Controller
@RequestMapping("/api/order/pay")
@Api(tags = "支付宝支付接口")
public class AlipayApiController {
    @Autowired
    private AlipayService alipayService;

    @GetMapping("/{orderId}")
    public void pay(@PathVariable String orderId, HttpServletResponse response) {
        alipayService.createNative(response, orderId);
    }

    @GetMapping("/auth/status/{orderId}")
    @ResponseBody
    public Result<Object> orderStatus(@PathVariable String orderId) {
        System.out.println("***orderId: " + orderId);
        alipayService.orderStatus(orderId);
        return Result.ok();
    }
}
