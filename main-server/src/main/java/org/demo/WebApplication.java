package org.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * 开启定时任务，本系统不打算布置为分布式，所以也不需要花里胡哨的quartz和xxl-job
 */
@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "org.demo.mapper")
public class WebApplication {

    /**
     * 注入RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
