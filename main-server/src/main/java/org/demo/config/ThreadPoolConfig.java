package org.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class ThreadPoolConfig {

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private static final int QUEUE_CAPACITY = 100;

    private static final String THREAD_PREFIX = "spring - pool - ";

    private static final int KEEP_ALIVE_SECONDS = 60;

    @Bean
    public ThreadPoolTaskExecutor executor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //创建核心线程数量
        executor.setCorePoolSize(CORE_POOL_SIZE + 1);
        //最大核心线程数
        executor.setMaxPoolSize(CORE_POOL_SIZE * 2);
        //队列长度（超过会创建工作线程）
        executor.setQueueCapacity(QUEUE_CAPACITY);
        //线程名称前缀，用于监控识别
        executor.setThreadNamePrefix(THREAD_PREFIX);
        //设置线程保持活跃的时间（默认：60）
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        //销毁线程池前先等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //设置任务拒绝策略, 在调用execute方法的线程中直接执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
