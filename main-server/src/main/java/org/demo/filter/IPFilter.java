package org.demo.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.util.IPUtil;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 通过redis实现IP限流，过滤器类名前需要排序，因为WebFilter注解底层只能通过类名进行排序
 */
@Slf4j
@Order(0)
//@Component
@RequiredArgsConstructor
public class IPFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String IP_PREFIX = "ip:";

    private static final int MAXIMUM_VISIT = 100;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("【IP-Filter】 start success");
        String ip = IP_PREFIX + IPUtil.getIpAddress(request);

        // 不会存在一个IP永远出不来的情况，因为每次后续访问都有机会重新加上过期时间。有必要保证原子性吗？没必要，因为这会大大降低访问速度，即使过期时间后加上也无所谓，多一次少一次没关系。
        Long value = stringRedisTemplate.opsForValue().increment(ip);
        stringRedisTemplate.expire(ip, 1, TimeUnit.SECONDS);

        if (value >= MAXIMUM_VISIT) {
            log.warn("【恶意IP】: {}", ip);
            return;
        }
        filterChain.doFilter(request, response);
    }

}
