package org.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    /**
     * 交换机配置
     */
    @Bean(value = "demoExchange")
    public Exchange demoExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.DEMO_EXCHANGE).durable(true).build();
        //配置持久化, 不配置自动删除，确保服务宕机但mq不受影响
    }

    @Bean(value = "barrageQueue")
    public Queue barrageQueue() {
        return QueueBuilder.durable(RabbitMQConstant.BARRAGE_QUEUE).ttl((int) (30 * RabbitMQConstant.MINUTE)).build();
        //队列统一过期
    }

    /**
     * 绑定关系配置
     */
    @Bean
    public Binding queueBindExchange(@Qualifier("demoExchange") Exchange exchange, @Qualifier("barrageQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstant.BARRAGE_ROUTING_KEY).noargs();
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //return(回退)模式配置
        rabbitTemplate.setMandatory(true);
        //设置发送消息时的强制标志；仅在提供了returnCallback的情况下适用。
        rabbitTemplate.setReturnsCallback((message) -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rabbitTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message.getMessage());
            log.warn("消息发送失败: {}, 重试中", message);
        });
        return rabbitTemplate;
    }

}
