package org.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.demo.RedisClient;
import org.demo.RedisConstant;
import org.demo.constant.EntityConstant;
import org.demo.dto.LoginDto;
import org.demo.dto.RegisterDto;
import org.demo.mapper.UserMapper;
import org.demo.pojo.base.GlobalRuntimeException;
import org.demo.pojo.User;
import org.demo.util.IPUtil;
import org.demo.util.JwtProvider;
import org.demo.util.ThreadHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@NotNull
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    // 通过手机号获得的验证码，在这里写死为123456
    private final static String SECRET_CODE = "123456";

    private final JwtProvider jwtProvider;

    private final UserMapper userMapper;

    private final RedisClient redisClient;

    public String register(RegisterDto registerDto, HttpServletRequest request) throws Exception {
        // 先验证验证码是否正确
        verifyCode(registerDto.getVerifyCode(), request);
        // 再验证注册码是否正确
        if (!registerDto.getRegisterCode().equals(SECRET_CODE))
            throw GlobalRuntimeException.of("手机验证码输入错误，请重新输入");
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(registerDto.getPassword().getBytes(StandardCharsets.UTF_8)));
        // 随机生成昵称
        user.setNickname(EntityConstant.NICKNAME_PREFIX + RandomStringUtils.randomAscii(16));
        if (userMapper.insert(user) != 1)
            throw GlobalRuntimeException.of("手机号已存在");
        // 将对象缓存入ThreadHolder
        ThreadHolder.setUser(user);
        // 重新查询user，写入SQL默认值，写入Redis缓存
        user = userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        redisClient.setWithExpiredTime(RedisConstant.CACHE_USER_USERNAME, user.getUsername(), user, 30L, TimeUnit.DAYS);
        return jwtProvider.create(user.getUsername(), 30);
    }


    public String authenticate(LoginDto loginDTO, HttpServletRequest request) throws Exception {

        verifyCode(loginDTO.getVerifyCode(), request);

        String password = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        String username = loginDTO.getUsername();
        // 利用电话号码查找
        User user = userMapper.selectOne(new QueryWrapper<User>().select().eq(true, "username", username));
        // 若失败，则利用邮箱查找
        if (user == null)
            user = userMapper.selectOne(new QueryWrapper<User>().select().eq(true, "email", username));
        if (user == null)
            throw GlobalRuntimeException.of("手机号码/邮箱未注册");

        // 设置线程对象
        if (!password.equals(user.getPassword()))
            throw GlobalRuntimeException.of("密码错误");
        ThreadHolder.setUser(user);
        // 设置用户缓存
        redisClient.setWithExpiredTime(RedisConstant.CACHE_USER_USERNAME, user.getUsername(), user, 30L, TimeUnit.DAYS);
        return jwtProvider.create(username, 30);
    }

    private void verifyCode(String verifyCode, HttpServletRequest request) throws Exception {
        verifyCode = verifyCode.toLowerCase(Locale.ROOT);
        String ip = IPUtil.getIpAddress(request);
        String code = redisClient.get(RedisConstant.VERIFY_CODE_IP, ip, String.class);
        if (code == null)
            throw GlobalRuntimeException.of("验证码已过期");
        if (!code.equals(verifyCode))
            throw GlobalRuntimeException.of("验证码错误");
    }
}
