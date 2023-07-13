package org.demo.test;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.demo.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SpringBootTest
public class MinioClientTest {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioService minioService;

    @Test
    public void test_upload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("image")
                        .object("/1/desk.png")
                        .filename("C:\\Users\\Administrator\\Pictures\\133672c019a406cdf4e3f39002acf13.png")
                        .build());
    }

    @Test
    public void test_download() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder().bucket("image").object("/1/desk.png").build());
        File file = new File("C:\\Users\\Administrator\\Desktop\\133672c019a406cdf4e3f39002acf13.png");
        OutputStream os = new FileOutputStream(file);
        response.transferTo(os);
        os.flush();
        os.close();
    }

    @Test
    public void service_test() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        try {
            FileOutputStream os = new FileOutputStream("C:\\Users\\Administrator\\Videos\\Captures\\test.mp4");
            // 下载测试
            minioService.download("video", "双人成行 2023-03-17 21-11-09.mp4", os);
            // 删除测试
            minioService.remove("common", "/desk.png");
            os.close();
        } catch (Exception e) {

        }
    }
}