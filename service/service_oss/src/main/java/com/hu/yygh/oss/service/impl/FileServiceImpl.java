package com.hu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.hu.yygh.oss.service.FileService;
import com.hu.yygh.oss.utils.ConstantOssPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author suhu
 * @createDate 2022/2/25
 */
@Service
public class FileServiceImpl implements FileService {

    @Override
    public String upload(MultipartFile file) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantOssPropertiesUtil.ENDPOINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantOssPropertiesUtil.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtil.SECRET;
        // 填写Bucket名称
        String bucketName = ConstantOssPropertiesUtil.BUCKET;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String date = new DateTime().toString("yyyy/MM/dd");
            fileName = date + "/" + uuid + fileName;
            // 创建PutObject请求。
            ossClient.putObject(bucketName, fileName, inputStream);
            return "https://" + bucketName + "." + endpoint + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
