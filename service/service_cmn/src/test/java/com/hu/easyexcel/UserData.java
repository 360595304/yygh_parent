package com.hu.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author suhu
 * @createDate 2022/2/10
 */
@Data
public class UserData {
    @ExcelProperty("用户编号")
    private Long uid;
    @ExcelProperty("用户姓名")
    private String name;
}
