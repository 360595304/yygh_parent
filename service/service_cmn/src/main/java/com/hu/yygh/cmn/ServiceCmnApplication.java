package com.hu.yygh.cmn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author suhu
 * @createDate 2022/2/10
 */
@SpringBootApplication
@ComponentScan("com.hu")
public class ServiceCmnApplication {
    public static void main(String[] args){
        SpringApplication.run(ServiceCmnApplication.class, args);
    }
}
