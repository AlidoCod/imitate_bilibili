package org.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.demo.RabbitMQConstant;
import org.demo.RedisClient;
import org.demo.RedisConstant;
import org.demo.controller.vo.JsonBean;
import org.demo.core.pojo.Barrage;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BarrageService {

    private final RabbitTemplate rabbitTemplate;

    private final RedisClient redisClient;

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /*
     * 前缀采用userId和timestamp确保一秒内用户只能发一条弹幕
     * */
    public JsonBean<Void> sent(Long userId, Long videoId, String message, String videoTime) throws JsonProcessingException {
        Long id = stringRedisTemplate.opsForValue().increment(RedisConstant.BARRAGE_LOCK_ID);
        /*
         * 避免高并发越界，当Long到达最大值的一半时，就直接重置，无论并发数多大，都不可能瞬间突破到Long值
         * 更新数据时，需要上锁，避免多线程同时满足条件，出现重复ID
         * 局部锁其实影响不大
         *
         * 但是考虑到，我设置了消息失败机制，一旦消息ID重复，那么消息消费也会失败，至多只能消费一条ID一致的弹幕
         * 因此，不必上锁，弹幕并不需要非常强的可达性。
         * */

        if (id / (Long.MAX_VALUE / 2) == 1)
            stringRedisTemplate.opsForValue().set(RedisConstant.BARRAGE_LOCK_ID, String.valueOf(0));

        Barrage barrage = new Barrage();
        barrage.setUserId(userId);
        barrage.setVideoId(videoId);
        barrage.setContent(message);
        barrage.setVideoTime(videoTime);
        rabbitTemplate.convertAndSend(RabbitMQConstant.DEMO_EXCHANGE, RabbitMQConstant.BARRAGE_ROUTING_KEY, objectMapper.writeValueAsString(barrage), new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setHeader("msg-id", id);
                return message;
            }
        });
        //有1s内发出多个消息的可能，但是由于消费完后，会对删除key，所以1s就算收到重复的或者大量的消息，也不会进行重复的消费。
        return redisClient.setIfAbsent(RedisConstant.BARRAGE_LOCK_ID, String.valueOf(id), "", 30L, TimeUnit.MINUTES) ?
                JsonBean.success() : JsonBean.fail("服务器内部弹幕ID重复, 稍等片刻即可");
    }

}
