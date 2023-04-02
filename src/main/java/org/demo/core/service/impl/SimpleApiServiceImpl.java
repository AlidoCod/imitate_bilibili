package org.demo.core.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.core.constant.RedisConstant;
import org.demo.core.service.SimpleApiService;
import org.demo.core.util.IPUtil;
import org.demo.core.util.RedisClient;
import org.demo.core.util.VerifyCodeUtil;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleApiServiceImpl implements SimpleApiService {

    private final RedisClient redisClient;

    private final ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void generateVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ip = IPUtil.getIpAddress(request);
        OutputStream os = response.getOutputStream();
        // 如果image为null，就发送图片、后再存入redis；否则则解码image、传送到前端
        String code = VerifyCodeUtil.generateVerifyCode(4);
        // 生成图片
        BufferedImage bufferedImage = VerifyCodeUtil.getBufferedImage(200, 80, code);
        // 发送图片
        ImageIO.write(bufferedImage, "jpg", os);
        // 异步加密图片并存入redis
        redisClient.setWithExpiredTime(RedisConstant.VERIFY_CODE_IP, ip, code.toLowerCase(Locale.ROOT), 15L, TimeUnit.MINUTES);
        os.flush();
    }

}
