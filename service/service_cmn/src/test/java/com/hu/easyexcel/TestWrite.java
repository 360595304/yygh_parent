package com.hu.easyexcel;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author suhu
 * @createDate 2022/2/10
 */
public class TestWrite {
    public static void main(String[] args) {
        List<UserData> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            UserData userData = new UserData();
            userData.setUid((long) i);
            userData.setName("yyz" + i);
            list.add(userData);
        }
        EasyExcel.write("C:\\Users\\Administrator\\Desktop\\01.xlsx", UserData.class).sheet("用户信息")
                .doWrite(list);
    }
}
