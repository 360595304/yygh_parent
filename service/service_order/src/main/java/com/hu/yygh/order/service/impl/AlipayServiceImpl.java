package com.hu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hu.yygh.enums.OrderStatusEnum;
import com.hu.yygh.enums.PaymentTypeEnum;
import com.hu.yygh.model.order.OrderInfo;
import com.hu.yygh.model.order.PaymentInfo;
import com.hu.yygh.order.service.AlipayService;
import com.hu.yygh.order.service.OrderService;
import com.hu.yygh.order.service.PaymentService;
import com.hu.yygh.order.utils.ConstantPropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/3/3
 */
@Service
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void createNative(HttpServletResponse httpResponse, String orderId) {
        try {
            //根据id获取订单信息
            // 保存交易记录
            OrderInfo order = orderService.getById(orderId);
            paymentService.savePaymentInfo(order, PaymentTypeEnum.ALIPAY.getStatus());
//            if (StringUtils.isEmpty(paymentId)) {
//                QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
//                queryWrapper.eq("order_id", orderId);
//                PaymentInfo paymentInfo = paymentService.getOne(queryWrapper);
//                paymentId = String.valueOf(paymentInfo.getId());
//            }
            //1、设置参数
            Map<String, Object> paramMap = new HashMap<>();
            Map<String, String> body = new HashMap<>();
            String message = order.getReserveDate() + "就诊" + order.getDepname();
            body.put("message", message);
            paramMap.put("out_trade_no", order.getOutTradeNo());
            paramMap.put("total_amount", order.getAmount());
            paramMap.put("subject", order.getHosname());
            paramMap.put("body", JSONObject.toJSONString(body));
            paramMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
//            paramMap.put("passback_params", );
//            Map<String, String> passbackParams = new HashMap<>();
//            passbackParams.put("payment_id", paymentId);
//            paramMap.put("passback_params", passbackParams);
            paramMap.put("sys_service_provider_id", "127.0.0.1");
//            paramMap.put("time_expire", new DateTime().plusMinutes(30).toString("yyyy-MM-dd HH:mm:ss"));


            AlipayClient alipayClient = new DefaultAlipayClient(
                    "https://openapi.alipaydev.com/gateway.do",
                    ConstantPropertiesUtils.APP_ID,
                    ConstantPropertiesUtils.APP_PRIVATE_KEY,
                    ConstantPropertiesUtils.FORMAT,
                    ConstantPropertiesUtils.CHARSET,
                    ConstantPropertiesUtils.ALIPAY_PUBLIC_KEY,
                    ConstantPropertiesUtils.SIGN_TYPE);  //获得初始化的AlipayClient
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest(); //创建API对应的request
            alipayRequest.setReturnUrl("");
            alipayRequest.setNotifyUrl(""); //在公共参数中设置回跳和通知地址
            alipayRequest.setBizContent(JSONObject.toJSONString(paramMap)); //填充业务参数
            String form = "";
            try {
                form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
//                redisTemplate.opsForValue().set(orderId, paymentId, 30, TimeUnit.MINUTES);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            httpResponse.setContentType("text/html;charset=utf-8");
            httpResponse.getWriter().write(form); //直接将完整的表单html输出到页面
            httpResponse.getWriter().flush();
            httpResponse.getWriter().close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void orderStatus(String orderId) {
        AlipayClient alipayClient = new DefaultAlipayClient(
                "https://openapi.alipaydev.com/gateway.do",
                ConstantPropertiesUtils.APP_ID,
                ConstantPropertiesUtils.APP_PRIVATE_KEY,
                ConstantPropertiesUtils.FORMAT,
                ConstantPropertiesUtils.CHARSET,
                ConstantPropertiesUtils.ALIPAY_PUBLIC_KEY,
                ConstantPropertiesUtils.SIGN_TYPE);  //获得初始化的AlipayClient
        //设置请求参数
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();

        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no = orderService.getOrder(orderId).getOutTradeNo();
        //支付宝交易号
//        String trade_no = ;
        //请二选一设置
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", out_trade_no);
        bizContent.put("query_options", new String[]{"send_pay_date", "body"});
        //bizContent.put("trade_no", "2014112611001004680073956707");
        alipayRequest.setBizContent(bizContent.toString());
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(alipayRequest);
            if (response.isSuccess()) {
                JSONObject result = JSONObject.parseObject(response.getBody());
                System.out.println(result);
                JSONObject alipayResponse = result.getJSONObject("alipay_trade_query_response");
                String tradeStatus = (String) alipayResponse.get("trade_status");
                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    OrderInfo orderInfo = orderService.getById(orderId);
                    orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
                    orderInfo.setUpdateTime(new Date());
                    orderService.updateById(orderInfo);
                    QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("order_id", orderId);
                    PaymentInfo paymentInfo = paymentService.getOne(queryWrapper);
                    paymentInfo.setTradeNo(alipayResponse.getString("trade_no"));
                    paymentInfo.setCallbackTime(new Date());
                    paymentInfo.setPaymentStatus(1);
                    paymentInfo.setCallbackContent(JSONObject.toJSONString(result));
                    paymentInfo.setUpdateTime(new Date());
                    paymentService.updateById(paymentInfo);
                }
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


    }
}
