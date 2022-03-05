package com.hu.yygh.order.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author suhu
 * @createDate 2022/3/3
 */
@Component
public class ConstantPropertiesUtils implements InitializingBean {

    @Value(value = "${alipay.appId}")
    private String appId;
    @Value(value = "${alipay.appPrivateKey}")
    private String appPrivateKey;
    @Value(value = "${alipay.format}")
    private String format;
    @Value(value = "${alipay.charset}")
    private String charset;
    @Value(value = "${alipay.alipayPublicKey}")
    private String alipayPublicKey;
    @Value(value = "${alipay.signType}")
    private String signType;

    public static String APP_ID;
    public static String APP_PRIVATE_KEY;
    public static String FORMAT;
    public static String CHARSET;
    public static String ALIPAY_PUBLIC_KEY;
    public static String SIGN_TYPE;

    @Override
    public void afterPropertiesSet() throws Exception {
        APP_ID = appId;
        APP_PRIVATE_KEY = appPrivateKey;
        FORMAT = format;
        CHARSET = charset;
        ALIPAY_PUBLIC_KEY = alipayPublicKey;
        SIGN_TYPE = signType;
    }
}
