package org.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.minio.errors.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.RedisClient;
import org.demo.RedisConstant;
import org.demo.constant.EntityConstant;
import org.demo.dto.file.MultipartFileParamDto;
import org.demo.dto.user.UserUpdateDto;
import org.demo.mapper.ImageMapper;
import org.demo.mapper.UserMapper;
import org.demo.pojo.GlobalRuntimeException;
import org.demo.pojo.Image;
import org.demo.pojo.User;
import org.demo.vo.Result;
import org.demo.vo.UserInformationVo;
import org.demo.util.JwtProvider;
import org.demo.util.ObjectConverter;
import org.demo.util.ThreadHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 注意：图片无论如何都不能删除，因为同一个图片可能被多个表绑定
 */
@Slf4j
@RequiredArgsConstructor
@Service("userService")
public class UserService {

    private final UserMapper userMapper;

    private final RedisClient redisClient;

    private final RestTemplate restTemplate;

    private final JwtProvider jwtProvider;

    private final ImageMapper imageMapper;

    private final ApplicationContext applicationContext;

    private final MinioService minioService;

    @Value("${server.socket}")
    private String socket;

    /**
     * 不要图一时之快使用User，后面找JsonIgnore的bug时两行泪，JsonIgnore会使得不被objectMapper序列化，而且是全局的
     * 查询当前会话的用户信息
     * @return
     */

    public Result<UserInformationVo> query() throws Exception{
        User user = ThreadHolder.getUser();
        UserInformationVo userVo = ObjectConverter.convert(user, UserInformationVo.class);
        return Result.successByData(userVo);
    }

    public void delete() throws Exception {
        String username = ThreadHolder.getUser().getUsername();
        redisClient.delete(RedisConstant.CACHE_USER_USERNAME, username);
        try {
            userMapper.delete(new QueryWrapper<User>().eq("username", username));
        } catch (Exception e) {
            e.printStackTrace();
            throw GlobalRuntimeException.of("用户删除失败，请联系管理员");
        }
    }

    public void update(UserUpdateDto vo) {
        User user = new User();
        user.setId(ThreadHolder.getUser().getId());
        user.setNickname(vo.getNickname());
        user.setEmail(vo.getEmail());
        user.setTags(vo.getTags());
        user.setSignature(vo.getSignature());
        try {
            redisClient.delete(RedisConstant.CACHE_USER_USERNAME, ThreadHolder.getUsername());
            userMapper.updateById(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw GlobalRuntimeException.of("更新失败，请联系管理员");
        }
    }

    /**
     * 更新完后也不能删除图片，避免其他表找不到图片，因为上传底层实现了秒传。
     */
    @Transactional
    public Result<Void> coverUpdate(MultipartFile file, MultipartFileParamDto dto) throws IOException {
        // 移除测试md5字符串出现的换行符
        String md5 = dto.getMd5().substring(0, 32);
        // 避免用户重复更新相同头像
        Image image = imageMapper.selectById(ThreadHolder.getUser().getImageId());
        if (image != null && image.getMd5().equals(md5))
            return Result.success();

        String username = ThreadHolder.getUsername();

        // 远程调用URL
        String url = socket + "/file/image/upload";

        // 创建token过验证
        HttpHeaders httpHeaders = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("multipart/form-data");
        httpHeaders.setContentType(type);
        httpHeaders.setContentLength(file.getSize());
        httpHeaders.add("token", jwtProvider.create(username));

        // 添加参数
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        File tempFile = File.createTempFile("111", "222");
        file.transferTo(tempFile);
        FileSystemResource resource = new FileSystemResource(tempFile);
        form.add("image", resource);
        form.add("md5", md5);
        form.add("suffix", dto.getSuffix());

        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, httpHeaders);

        User user = new User();
        user.setId(ThreadHolder.getUser().getId());

        log.debug(Thread.currentThread().toString());

        UserService userService = (UserService) applicationContext.getBean("userService");

        /**
         * 为什么把方法提取出来？因为Spring事务的Connection是存储在ThreadLocal中的，所以多线程会使得事务失效，因此需要将需要事务的方法提取出来。
         * 为什么需要自己注入自己再调用事务方法？因为this自调用的this指针没有被动态代理，是没有事务特性的！
        */
        userService.extracted(username, url, tempFile, files, user);
        return Result.success();
    }

    @Async
    @Transactional
    public void extracted(String username, String url, File tempFile, HttpEntity<MultiValueMap<String, Object>> files, User user) {
        try {
            log.debug(Thread.currentThread().toString());
            Result jsonBean = restTemplate.postForObject(url, files, Result.class);
            // 准备插入imageId
            user.setImageId(Long.valueOf((String) jsonBean.getData()));
            userMapper.updateById(user);
            redisClient.delete(RedisConstant.CACHE_USER_USERNAME, username);
        }catch (Exception ex) {
            tempFile.delete();
            log.error("远程调用失败", ex);
        }
    }

    public Result<Void> downloadCover(HttpServletResponse response) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Long imageId = ThreadHolder.getUser().getImageId();
        Image image = imageMapper.selectById(imageId);
        if (image == null)
            return Result.fail("未设置头像");
        ServletOutputStream outputStream = response.getOutputStream();
        minioService.download(EntityConstant.IMAGE_BUCKET, image.getImagePath(), outputStream);
        outputStream.flush();
        return Result.success(image.getMd5());
    }

}
