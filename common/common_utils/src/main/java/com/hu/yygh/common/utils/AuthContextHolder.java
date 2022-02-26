package com.hu.yygh.common.utils;

import com.hu.yygh.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * @author suhu
 * @createDate 2022/2/25
 */
public class AuthContextHolder {
    public static Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        return JwtHelper.getUserId(token);
    }

    public static String getUserName(HttpServletRequest request) {
        String token = request.getHeader("token");
        return JwtHelper.getUserName(token);
    }
}
