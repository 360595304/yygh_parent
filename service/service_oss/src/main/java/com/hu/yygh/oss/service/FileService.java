package com.hu.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author suhu
 * @createDate 2022/2/25
 */
public interface FileService {
    String upload(MultipartFile file);
}
