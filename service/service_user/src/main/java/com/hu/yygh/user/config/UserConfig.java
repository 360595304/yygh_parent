package com.hu.yygh.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author suhu
 * @createDate 2022/2/21
 */
@Configuration
@MapperScan("com.hu.yygh.user.mapper")
public class UserConfig {

}
