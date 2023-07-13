package org.demo.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.demo.service.BarrageService;
import org.demo.vo.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BarrageServiceTest {

    @Autowired
    BarrageService barrageService;

    /**
     * 测试弹幕发送功能
     */
    @Test
    void sent() throws JsonProcessingException {
        Result<Void> result = barrageService.sent(1641708143060385794L, 1L, "Hello World", "12:00");
        Assertions.assertEquals(Result.success().getCode(), result.getCode());
    }
}