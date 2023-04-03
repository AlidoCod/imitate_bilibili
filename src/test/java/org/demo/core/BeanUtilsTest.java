package org.demo.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.controller.vo.UserVo;
import org.demo.core.pojo.User;
import org.demo.core.util.ObjectConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class BeanUtilsTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void converterTest() throws JsonProcessingException {
        User user = new User();
        user.setEmail("xxxx");
        UserVo userVo = ObjectConverter.convert(user, UserVo.class);
        System.out.println(userVo);
    }
}
