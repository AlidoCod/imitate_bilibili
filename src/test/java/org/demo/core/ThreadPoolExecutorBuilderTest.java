package org.demo.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest
public class ThreadPoolExecutorBuilderTest {

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Test
    public void thread_pool_test() {
        threadPoolExecutor.execute(() -> System.out.println("123"));
    }
}
