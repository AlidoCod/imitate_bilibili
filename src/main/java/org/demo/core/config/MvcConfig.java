package org.demo.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.aop.JwtAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 排除不需认证的资源路径，其余全部拦截进入Token拦截器
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    /**
     * 不需要拦截的请求，直接放行
     * /*表示放行所有一级请求
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/swagger-resources/**", "/webjars/**",
                        "/v3/**", "/swagger-ui/**", "/null/**", "/api/**", "/file/**", "/*");
    }

    /**
     * 实现静态资源映射, 让doc.html能找到有关它的静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
