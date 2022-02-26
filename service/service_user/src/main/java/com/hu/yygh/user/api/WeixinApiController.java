package com.hu.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.hu.yygh.common.exception.YyghException;
import com.hu.yygh.common.helper.JwtHelper;
import com.hu.yygh.common.result.Result;
import com.hu.yygh.common.result.ResultCodeEnum;
import com.hu.yygh.model.user.UserInfo;
import com.hu.yygh.user.service.UserInfoService;
import com.hu.yygh.user.utils.ConstantPropertiesUtil;
import com.hu.yygh.user.utils.HttpClientUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/23
 */
@Controller
@RequestMapping("/api/ucenter/wx")
@Api(tags = "微信登陆接口")
@Slf4j
public class WeixinApiController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @ApiOperation("获取微信登录参数")
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result<Map<String, Object>> genQrConnect(HttpSession session) throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis() + "");//System.currentTimeMillis()+""
        return Result.ok(map);
    }

    @ApiOperation("微信扫描后的回调")
    @GetMapping("/callback")
    public String callBack(String code, String state) {
        System.out.println("code: " + code);
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {
            log.error("非法回调请求");
            throw new YyghException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }

        //使用code和appid以及appscrect换取access_token
        String baseAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                "?appid=%s" +
                "&secret=%s" +
                "&code=%s" +
                "&grant_type=authorization_code";

        String accessTokenUrl = String.format(baseAccessTokenUrl,
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);

        String result;
        try {
            result = HttpClientUtils.get(accessTokenUrl);
        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        System.out.println("使用code换取的access_token结果 = " + result);

        JSONObject resultJson = JSONObject.parseObject(result);
        if (resultJson.getString("errcode") != null) {
            log.error("获取access_token失败：" + resultJson.getString("errcode") + resultJson.getString("errmsg"));
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        String accessToken = resultJson.getString("access_token");
        String openId = resultJson.getString("openid");
        log.info(accessToken);
        log.info(openId);

        //根据access_token获取微信用户的基本信息
        //先根据openid进行数据库查询
        UserInfo userInfo = userInfoService.getByOpenid(openId);
        // 如果没有查到用户信息,那么调用微信个人信息获取的接口
        if (null == userInfo) {
            //如果查询到个人信息，那么直接进行登录
            //使用access_token换取受保护的资源：微信的个人信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
            String resultUserInfo;
            try {
                resultUserInfo = HttpClientUtils.get(userInfoUrl);
            } catch (Exception e) {
                throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }
            System.out.println("使用access_token获取用户信息的结果 = " + resultUserInfo);

            JSONObject resultUserInfoJson = JSONObject.parseObject(resultUserInfo);
            if (resultUserInfoJson.getString("errcode") != null) {
                log.error("获取用户信息失败：" + resultUserInfoJson.getString("errcode") + resultUserInfoJson.getString("errmsg"));
                throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }

            //解析用户信息
            String nickname = resultUserInfoJson.getString("nickname");
            String headimgurl = resultUserInfoJson.getString("headimgurl");

            userInfo = new UserInfo();
            userInfo.setOpenid(openId);
            userInfo.setNickName(nickname);
            userInfo.setStatus(1);
            userInfoService.save(userInfo);
        }

        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        // 判断手机号是否为空，若已绑定手机号则返回空openid
        if (StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            map.put("openid", "");
        }
        // 生成token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return "redirect:" + ConstantPropertiesUtil.YYGH_BASE_URL +
                "/weixin/callback?token=" + map.get("token") +
                "&openid=" + map.get("openid") + "&name="
                + URLEncoder.encode((String) map.get("name"));
    }
}

