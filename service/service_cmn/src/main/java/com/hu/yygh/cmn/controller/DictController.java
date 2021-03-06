package com.hu.yygh.cmn.controller;

import com.hu.yygh.cmn.service.DictService;
import com.hu.yygh.common.result.Result;
import com.hu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author suhu
 * @createDate 2022/2/10
 */
@RestController
@RequestMapping("/admin/cmn/dict")
@Api(tags = "数据字典接口")
public class DictController {
    @Autowired
    private DictService dictService;

    @ApiOperation("根据数据id查询子数据列表")
    @GetMapping("/findChildData/{id}")
    public Result<Object> findChildData(@PathVariable Long id) {
        List<Dict> list = dictService.findChildData(id);
        return Result.ok(list);
    }

    @ApiOperation("根据dictCode查询子数据列表")
    @GetMapping("/findByDictCode/{dictCode}")
    public Result<Object> findByDictCode(@PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }

    @ApiOperation("导出数据字典")
    @GetMapping("/exportData")
    public void exportDict(HttpServletResponse response) {
        dictService.exportDictData(response);
    }

    @ApiOperation("导入数据字典")
    @PostMapping("/importData")
    public void importData(MultipartFile file) {
        dictService.importDictData(file);
    }

    @ApiOperation("根据dictcode和value查询")
    @GetMapping("/getName/{dictCode}/{value}")
    public String getName(@PathVariable String dictCode,
                          @PathVariable String value) {
        return dictService.getDictName(dictCode, value);
    }

    @ApiOperation("根据value查询")
    @GetMapping("/getName/{value}")
    public String getName(@PathVariable String value) {
        return dictService.getDictName("", value);
    }

}
