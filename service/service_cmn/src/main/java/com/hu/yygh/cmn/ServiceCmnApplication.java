package com.hu.yygh.cmn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author suhu
 * @createDate 2022/2/10
 */
@SpringBootApplication
@ComponentScan("com.hu")
@EnableDiscoveryClient
public class ServiceCmnApplication {
    public static void main(String[] args){
        SpringApplication.run(ServiceCmnApplication.class, args);
    }
}
