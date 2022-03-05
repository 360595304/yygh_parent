package com.hu.yygh.order.service;

import javax.servlet.http.HttpServletResponse;

public interface AlipayService {
    void createNative(HttpServletResponse httpResponse, String orderId);

    void orderStatus(String orderId);
}
