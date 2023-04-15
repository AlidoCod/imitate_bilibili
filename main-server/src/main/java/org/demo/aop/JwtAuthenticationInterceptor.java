package org.demo.aop;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.RedisClient;
import org.demo.RedisConstant;
import org.demo.controller.vo.JsonBean;
import org.demo.core.dao.UserMapper;
import org.demo.filter.xss.XssHttpServletRequestWrapper;
import org.demo.core.pojo.User;
import org.demo.core.pojo.enums.ResponseEnum;
import org.demo.util.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 权限资源的认证过滤器
 * 通过此拦截器的有且仅有以下几种情况
 * 1. 同一个线程的请求，固本身存在ThreadHolder
 * 2. 可以解析出Token获取ThreadHolder
 * 因此可以通过此拦截器的都是认证通过的用户
 * 只要用户有Token，那么就一定有ThreadHolder，也就不用担心存在缓存不一致的问题，只要记得删缓存即可。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    /**
     * 需要每次收到请求的时候，过滤器都处于活动状态
     * 因此每次用户发送请求时希望过滤器被触发并完成要做的所有工作
     */
    private final JwtProvider jwtProvider;

    private final RedisClient redisClient;

    private final ResponseHelper responseHelper;

    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = XssHttpServletRequestWrapper.getOrgRequest(request).getHeader("token");
        // 如果当前线程中存在threadHolder那么无需token直接放行
        if (ThreadHolder.getUser() != null)
            return true;
        // 没有token，还请求，返回没有权限
        if (token == null) {
            responseHelper.writeObject(response, JsonBean.responseEnum(ResponseEnum.HTTP_STATUS_401));
            return false;
        }
        String username = jwtProvider.parse(token);
        if (username != null) {
            // 封装为threadHolder
            User user = redisClient.getWithNonHotSpotKey(RedisConstant.CACHE_USER_USERNAME, username, User.class,
                    () -> userMapper.selectOne(new QueryWrapper<User>().eq("username", username))
                    , 30L, TimeUnit.DAYS);
            ThreadHolder.setUser(user);
            return true;
        }
        // 剩下的说明token无效
        responseHelper.writeObject(response, "token已失效");
        log.info("token已失效");
        return false;
    }

}