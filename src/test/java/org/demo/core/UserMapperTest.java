package org.demo.core;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.demo.core.dao.UserMapper;
import org.demo.core.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    UserMapper userMapper;

    @Test
    public void test() {
        User user = new User();
        user.setUsername("test");
        System.out.println(userMapper.selectOne(new QueryWrapper<User>().eq("username", "test")));
    }
}
