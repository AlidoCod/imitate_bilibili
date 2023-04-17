package org.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan(basePackages = "org.demo.mapper")
public class WebSocketApplication {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebSocketApplication.class, args);
        new WebsocketServer(81).run();
    }
}
