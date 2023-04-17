package org.demo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.demo.pojo.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper extends BaseMapper<Comment> {
}
