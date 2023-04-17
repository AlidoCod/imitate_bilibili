package org.demo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.demo.pojo.User;
import org.demo.vo.UserVo;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UserMapper extends BaseMapper<User> {
    Page<UserVo> selectUserVoPage(@Param("page") Page<UserVo> page
            , @Param("set") Set<String> set);

}
