package com.hu.yygh.hosp.cofig;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author suhu
 * @createDate 2022/2/7
 */
@Configuration
@MapperScan("com.hu.yygh.hosp.mapper")
public class HospitalConfig {
}
