package com.hu.yygh.hosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author suhu
 * @createDate 2022/2/7
 */
@SpringBootApplication
@ComponentScan("com.hu")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.hu")
public class ServiceHospApplication {
    public static void main(String[] args){
        SpringApplication.run(ServiceHospApplication.class, args);
    }
}
