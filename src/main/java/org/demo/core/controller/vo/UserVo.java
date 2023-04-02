package org.demo.core.controller.vo;

import lombok.Data;
import org.demo.core.entity.enums.Tag;

import java.util.List;

@Data
public class UserVo {

    String username;
    String nickname;
    String email;
    List<Tag> tags;
    String signature;
}