package org.demo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 需要解决的问题就是如何对channels进行细分
 * 先明白怎么触发写事件，只要我有channels我就能触发写事件！
 * 所以说，只需要用读事件来对channels进行分类就行了！
 * ChannelGroup的优点，就是不需要我去专门管理channel
 */
@Slf4j
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    public static Map<Long, ChannelGroup> channelGroupMap = new ConcurrentHashMap<>(1000);
    //key: videoId, value: ChannelGroup, 最多一千个视频别太过分哈

    /*
    * 建立连接时，触发
    * */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
    }

    /*
    * 连接断开时
    * */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.debug("channel inactive, disconnect, IP: {}", ctx.channel().remoteAddress());
    }

    /*
    * 活跃连接
    * */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    /*
    * 不活跃的连接
    * */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame("400"));
        log.debug("channel inactive, disconnect, IP: {}", ctx.channel().remoteAddress());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        // 当出现异常就关闭连接
        log.error("弹幕系统channel输出异常, 异常的IP地址为: {}", channel.remoteAddress(), cause);
        channel.writeAndFlush(new TextWebSocketFrame("500"));
        ctx.close();
    }

    /*
    * 对弹幕连接进行分组
    * */
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) {
        Channel channel = channelHandlerContext.channel();
        Long videoId = Long.valueOf(textWebSocketFrame.text());
        log.debug("channel videoId: {}", videoId);
        ChannelGroup defaultGroup = channelGroupMap.getOrDefault(videoId, new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        defaultGroup.add(channel);
        channelGroupMap.put(videoId, defaultGroup);
        log.debug(channelGroupMap.get(videoId).toString());
        channel.writeAndFlush(new TextWebSocketFrame("200"));
        //websocket 协议格式
    }
}