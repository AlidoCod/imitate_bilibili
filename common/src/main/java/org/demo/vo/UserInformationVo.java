package org.demo.vo;

import lombok.Data;
import org.demo.pojo.base.Tag;

import java.util.List;

@Data
public class UserInformationVo {

    String username;
    String nickname;
    String email;
    List<Tag> tags;
    String signature;
}