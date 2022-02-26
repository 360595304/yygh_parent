package com.hu.yygh.oss.controller;

import com.hu.yygh.common.result.Result;
import com.hu.yygh.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author suhu
 * @createDate 2022/2/25
 */
@RestController
@RequestMapping("/api/oss/file")
@Api(tags = "阿里云OSS文件上传接口")
public class FileApiController {

    @Autowired
    private FileService fileService;

    @ApiOperation("文件上传")
    @PostMapping("/fileUpload")
    public Result<String> fileUpload(MultipartFile file) {
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}
