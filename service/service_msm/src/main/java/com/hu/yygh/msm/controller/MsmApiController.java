package com.hu.yygh.msm.controller;

import com.hu.yygh.msm.service.MsmService;
import com.hu.yygh.msm.utils.RandomUtil;
import com.hu.yygh.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author suhu
 * @createDate 2022/2/21
 */
@RestController
@RequestMapping("/api/msm")
@Api(tags = "短信接口")
public class MsmApiController {
    @Autowired
    private MsmService msmService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @ApiOperation("发送验证码")
    @GetMapping("/send/{phone}")
    public Result<Object> sendCode(@PathVariable String phone) {
        String code = redisTemplate.opsForValue().get(phone);
        // 验证码还未失效
        if (!StringUtils.isEmpty(code)) {
            return Result.ok();
        }
        // 生成6位验证码
        code = RandomUtil.getSixBitRandom();
        // 发送验证码
        boolean isSend = msmService.send(phone, code);
        //将验证码存放在redis中
        if (isSend) {
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return Result.ok();
        } else {
            return Result.fail().message("发送短信失败");
        }
    }
}
