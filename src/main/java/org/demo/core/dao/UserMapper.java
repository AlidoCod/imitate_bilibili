package org.demo.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.demo.core.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper extends BaseMapper<User> {

    List<User> findUserByUserDao(User user);

    Integer insertUserByUserDao(User user);
}
