package com.hu.yygh.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hu.yygh.msm.service.MsmService;
import com.hu.yygh.msm.utils.HttpUtils;
import com.hu.yygh.vo.msm.MsmVo;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/2/21
 */
@Service
public class MsmServiceImpl implements MsmService {
    @Override
    public boolean send(String phone, String code) {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "897d6d96709e445db2aaee88ab7810e0";
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:" + code + ",**minute**:5");
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
//            String entity = response.getEntity().toString();
//            System.out.println(entity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean send(MsmVo msmVo) {
        if (!StringUtils.isEmpty(msmVo.getPhone())){
            return this.send(msmVo.getPhone(), msmVo.getParam(), msmVo.getTemplateCode());
        }
        return false;
    }

    private boolean send(String phone, Map<String, Object> params, String templateId) {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "897d6d96709e445db2aaee88ab7810e0";
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", phone);
        StringBuilder builder = new StringBuilder();
        params.forEach((key, value) -> {
            builder.append("**").append(key).append("**:").append(value).append(",");
        });
        builder.deleteCharAt(builder.length()-1);
        querys.put("param", builder.toString());
        querys.put("smsSignId", "123");
        querys.put("templateId", templateId);
//        Map<String, String> bodys = new HashMap<>();
        try {
//            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
//            System.out.println(response.toString());
            System.out.println("模拟发送短信：");
            System.out.println("host: " + host);
            System.out.println("path: " + path);
            System.out.println("method: " + method);
            System.out.println(JSONObject.toJSONString(headers));
            System.out.println(JSONObject.toJSONString(querys));
//            String entity = response.getEntity().toString();
//            System.out.println(entity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
