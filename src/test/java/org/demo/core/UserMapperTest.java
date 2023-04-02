package org.demo.core;

import org.demo.core.dao.UserMapper;
import org.demo.core.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    UserMapper userMapper;

    @Test
    public void test() {
        User user = new User();
        user.setUsername("test");
        System.out.println(userMapper.findUserByUserDao(user));
        System.out.println(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)).length());
    }
}
