package org.demo.core;

import lombok.extern.slf4j.Slf4j;
import org.demo.core.util.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

@Slf4j
@SpringBootTest
public class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @Test
    public void create_token_and_parse_token_test() {
        String token = jwtProvider.create("194183997@qq.com", 30);
        log.info(token);
        String username = jwtProvider.parse(token.toLowerCase(Locale.ROOT));
        log.info(username);
    }
}
