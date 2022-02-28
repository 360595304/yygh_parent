package com.hu.yygh.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author suhu
 * @createDate 2022/2/28
 */
@SpringBootApplication
@ComponentScan("com.hu")
@EnableDiscoveryClient
@EnableFeignClients("com.hu")
@MapperScan("com.hu.yygh.order.mapper")
public class ServiceOrderApplication {
    public static void main(String[] args){
        SpringApplication.run(ServiceOrderApplication.class, args);
    }
}
