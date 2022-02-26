package com.hu.yygh.oss.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author suhu
 * @createDate 2022/2/25
 */
@Component
public class ConstantOssPropertiesUtil implements InitializingBean {
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.secret}")
    private String secret;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    public static String ACCESS_KEY_ID;
    public static String SECRET;
    public static String BUCKET;
    public static String ENDPOINT;

    @Override
    public void afterPropertiesSet() throws Exception {
        ACCESS_KEY_ID = accessKeyId;
        SECRET = secret;
        BUCKET = bucket;
        ENDPOINT = endpoint;
    }
}
