package org.demo.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.demo.core.pojo.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper extends BaseMapper<Comment> {
}
