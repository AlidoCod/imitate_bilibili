package org.demo.core.service;

import io.minio.errors.*;
import jakarta.servlet.http.HttpServletResponse;
import org.demo.core.controller.dto.file.MultipartFileParamDto;
import org.demo.core.controller.dto.user.UserUpdateDto;
import org.demo.core.controller.vo.UserVo;
import org.demo.core.util.JsonBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface UserService {

    JsonBean<UserVo> query() throws Exception;

    void delete() throws Exception;

    void update(UserUpdateDto vo);

    JsonBean<Void> coverUpdate(MultipartFile file, MultipartFileParamDto dto) throws IOException;

    JsonBean<Void> downloadCover(HttpServletResponse response) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

}
