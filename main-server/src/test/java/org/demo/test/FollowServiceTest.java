package org.demo.test;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.demo.service.FollowService;
import org.demo.vo.UserVo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
//确保执行顺序正常
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class FollowServiceTest {

    @Autowired
    FollowService followService;

    @BeforeEach
    public void before() {
        log.debug("测试开始...");
    }


    @Order(1)
    @Test
    void follow() {
        followService.follow(1641708143060385794L, 1642776961031507970L);
        int size = getFollowers();
        Assertions.assertEquals(1, size);
    }

    @Order(2)
    @Test
    void isFollowed() {
        Boolean result = followService.isFollowed(1641708143060385794L, 1642776961031507970L);
        Assertions.assertEquals(true, result);
    }

    @Order(3)
    @Test
    int getFollowers() {
        Page<UserVo> page = followService.getFollowers(new Page<>(1, 10), 1641708143060385794L);
        return page.getRecords().size();
    }

    @Order(4)
    @Test
    void getFans() {
        Page<UserVo> page = followService.getFans(new Page<>(1, 10), 1642776961031507970L);
        Assertions.assertEquals(1, page.getRecords().size());
    }

    @Order(5)
    @Test
    void unfollow() throws InterruptedException {
        Thread.sleep(1000);
        followService.unfollow(1641708143060385794L, 1642776961031507970L);
        int size = getFollowers();
        Assertions.assertEquals(0, size);
    }
    @AfterEach
    public void after() {
        log.debug("测试结束");
    }
}