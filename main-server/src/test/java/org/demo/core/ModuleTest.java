package org.demo.core;

import org.demo.core.dao.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ModuleTest {

    @Autowired
    UserMapper userMapper;

    @Test
    public void  mapperTest() {
        userMapper.selectList(null).forEach(System.out::println);
    }
}
