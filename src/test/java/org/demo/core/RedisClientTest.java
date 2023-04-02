package org.demo.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.demo.core.util.RedisClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisClientTest {

    @Autowired
    RedisClient redisClient;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void function_test() throws JsonProcessingException {
        redisClient.setWithExpiredTime("test", ":1", "1234", 30L, TimeUnit.MINUTES);
        System.out.println(redisClient.getWithHotSpotKey("", "test1", String.class, null, 30L, TimeUnit.MINUTES));
    }
}
