package org.demo.test;

import lombok.extern.slf4j.Slf4j;
import org.demo.util.JwtProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    private static String cache;

    @Order(1)
    @Test
    public void create_token_test() {
        String token = jwtProvider.create("194183997@qq.com");
        log.info(token);
        this.cache = token;
    }

    @Order(2)
    @Test
    public void parse_token_test() {
        log.debug(cache);
        String username = jwtProvider.parse(cache);
        log.info(username);
        Assertions.assertEquals("194183997@qq.com", username);
    }
}