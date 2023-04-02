package org.demo.core;

import org.demo.core.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
public class FileServiceTest {

    @Autowired
    FileService fileService;

    @Test
    public void test_image_upload() throws IOException {
        File file = new File("C:\\Users\\Administrator\\Pictures\\133672c019a406cdf4e3f39002acf13.png");
        System.out.println(DigestUtils.md5DigestAsHex(new FileInputStream(file).readAllBytes()));
    }
}
