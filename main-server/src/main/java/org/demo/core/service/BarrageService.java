package org.demo.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.demo.core.constant.RabbitMQConstant;
import org.demo.core.constant.RedisConstant;
import org.demo.core.controller.vo.JsonBean;
import org.demo.core.pojo.Barrage;
import org.demo.core.util.RedisClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BarrageService {

    private final RabbitTemplate rabbitTemplate;

    private final RedisClient redisClient;

    private final ObjectMapper objectMapper;

    /*
    * 确保幂等性的思路是，10s内只允许发送一条弹幕，redis缓存时间为10s，若mq处理消息太慢，说明系统繁忙或者崩溃，那么就更不应该允许发送了，直接失败
    * */
    public JsonBean<Void> sent(Long userId, Long videoId, String message) throws JsonProcessingException {
        CorrelationData correlationData = new CorrelationData();
        String s = UUID.randomUUID().toString();
        correlationData.setId(s);
        Barrage barrage = new Barrage();
        barrage.setUserId(userId);
        barrage.setVideoId(videoId);
        barrage.setContent(message);
        rabbitTemplate.convertAndSend(RabbitMQConstant.DEMO_EXCHANGE, RabbitMQConstant.BARRAGE_ROUTING_KEY, objectMapper.writeValueAsString(barrage), correlationData);
        return redisClient.setIfAbsent(RedisConstant.BARRAGE_LOCK_USERID, String.valueOf(userId), "", 10L, TimeUnit.SECONDS) ?
                JsonBean.success() : JsonBean.fail("系统繁忙, 请10s后再尝试");
    }

}
