package org.demo.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.demo.filter.xss.XssHttpServletRequestWrapper;

import java.io.IOException;

@Slf4j
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class XSSFilter implements Filter {


    /**
     * 原理是对Http的请求进行再次封装，所有的http方法都是重写过后的方法
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("【XSSFilter】 start success");
        if (!((servletRequest instanceof HttpServletRequest) && (servletResponse instanceof HttpServletResponse))) {
            throw new ServletException("XSSFilter only supports HTTP requests");
        }
        XssHttpServletRequestWrapper wrappedRequest =
                new XssHttpServletRequestWrapper((HttpServletRequest) servletRequest);
        filterChain.doFilter(wrappedRequest, servletResponse);
    }

    // other methods
}