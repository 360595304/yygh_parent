package com.hu.easyexcel;

import com.alibaba.excel.EasyExcel;

/**
 * @author suhu
 * @createDate 2022/2/10
 */
public class TestRead {
    public static void main(String[] args) {
        String fileName = "C:\\Users\\Administrator\\Desktop\\01.xlsx";
        EasyExcel.read(fileName, UserData.class, new ExcelListener()).sheet().doRead();
    }
}
