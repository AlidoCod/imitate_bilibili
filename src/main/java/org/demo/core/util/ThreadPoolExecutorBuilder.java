package org.demo.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程重命名+全局线程异常捕获
 * 1. 如果想创建一个新的线程池，可以注入Builder，然后手动调用build方法
 * 2. 如果想复用线程池，可以直接注入ThreadPoolExecutor
 */
@Slf4j
@Component
public class ThreadPoolExecutorBuilder {

    @Value("${thread.prefix}")
    String prefix;
    @Value("${thread.capacity}")
    int capacity;
    int availableProcessors = Runtime.getRuntime().availableProcessors();

    @Bean(value = "threadPoolExecutor")
    public ThreadPoolExecutor build() {
        return new ThreadPoolExecutor(availableProcessors + 1, availableProcessors * 2, 60,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(capacity), new NamedThreadFactory(prefix), new ThreadPoolExecutor.AbortPolicy());
    }

    static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber;
        private final ThreadGroup group;
        private final String namePrefix;

        public NamedThreadFactory(String namePrefix) {
            this.threadNumber = new AtomicInteger(1);
            this.group = Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix + "-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
            t.setDaemon(false);
            t.setUncaughtExceptionHandler((thread, e) -> {
                log.error("【{}】catch exception", thread.getName(), e);
            });
            return t;
        }
    }

}
