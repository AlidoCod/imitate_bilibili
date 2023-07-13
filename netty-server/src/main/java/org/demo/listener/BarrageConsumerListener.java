package org.demo.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.RabbitMQConstant;
import org.demo.pojo.Barrage;
import org.demo.service.ConsumeBarrageService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class BarrageConsumerListener {

    private final ObjectMapper objectMapper;

    private final ConsumeBarrageService barrageService;

    @RabbitListener(queues = {RabbitMQConstant.BARRAGE_QUEUE})
    public void consumeMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //log.debug(message.toString());
            //multiple全部签收
            Barrage barrage = objectMapper.readValue(message.getBody(), Barrage.class);
            log.debug("message: {}", barrage);
            Long id = message.getMessageProperties().getHeader("msg-id");
            log.debug("msg-id: {}", id);
            barrageService.consume(id, barrage);
            channel.basicAck(deliveryTag, true);
        }catch (Exception e) {
            log.warn("消息签收失败", e);
            //错误消息是否重回队列
            channel.basicNack(deliveryTag, true, false);
        }
    }


}
