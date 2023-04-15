package org.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.demo.RedisClient;
import org.demo.RedisConstant;
import org.demo.TextWebSocketFrameHandler;
import org.demo.core.dao.BarrageMapper;
import org.demo.core.pojo.Barrage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BarrageService {

    private final BarrageMapper barrageMapper;

    private final RedisClient redisClient;

    private final ObjectMapper objectMapper;

    @Transactional
    public void consume(Long id, Barrage barrage) throws Exception{

        barrageMapper.insert(barrage);
        // 若插入失败，直接抛出异常拒绝签收消息
        ChannelGroup channels = TextWebSocketFrameHandler.channelGroupMap.get(barrage.getVideoId());
        channels.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(barrage)));
        redisClient.delete(RedisConstant.BARRAGE_LOCK_ID, String.valueOf(id));
        // 消息消费后删除
    }
}
