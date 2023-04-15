package org.demo.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio 对象存储文件系统连接
 */
@Configuration
public class MinioConfig {

    @Value("${minio.socket}")
    private String socket;
    @Value("${minio.username}")
    private String username;
    @Value("${minio.password}")
    private String password;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(socket)
                .credentials(username, password)
                .build();
    }
}
