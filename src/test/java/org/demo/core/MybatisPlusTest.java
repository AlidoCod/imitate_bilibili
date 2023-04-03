package org.demo.core;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.demo.core.dao.UserMapper;
import org.demo.core.pojo.User;
import org.demo.core.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MybatisPlusTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * 经过验证，mp自动实现了逻辑删除字段忽略
     * @throws Exception
     */
    @Test
    public void function_test() throws Exception {
        //userMapper.delete(new QueryWrapper<User>().eq("username", "admin"));
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", "admin"));
        System.out.println(user);
    }
}
