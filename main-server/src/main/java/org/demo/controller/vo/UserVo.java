package org.demo.controller.vo;

import lombok.Data;
import org.demo.core.pojo.enums.Tag;

import java.util.List;

@Data
public class UserVo {

    String username;
    String nickname;
    String email;
    List<Tag> tags;
    String signature;
}