package org.demo.core;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.demo.constant.EntityConstant;
import org.demo.mapper.UserMapper;
import org.demo.service.MinioService;
import org.demo.service.FollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

@Slf4j
@SpringBootTest
public class ModuleTest {

    @Autowired
    UserMapper userMapper;

    @Autowired
    MinioService minioService;

    @Autowired
    FollowService followService;

    @Test
    public void  mapperTest() {
        userMapper.selectList(null).forEach(System.out::println);
    }

    @Test
    public void chunkDownloadTest() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        InputStream inputStream = minioService.getDownloadInputStream(EntityConstant.VIDEO_BUCKET, "test.mp4");
        //inputStream.skipNBytes(1024 * 1024);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("C:\\Users\\Administrator\\Desktop\\test.mp4"));
        int limit = 0;
        byte[] buf = new byte[1024];
        while ((limit = inputStream.read(buf)) != -1) {
            bos.write(buf, 0, limit);
        }
    }

    @Test
    public void mpPageTest() {
        log.warn("???: {}", followService.getFollowers(new Page<>(1, 10), 1641708143060385794L).getRecords());
        log.warn("{}", userMapper.selectUserVoPage(new Page<>(1, 10), Set.of(String.valueOf(1641708143060385794L))).getRecords());
    }
}
