package org.demo.core.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.private-key}")
    private String privateKey;

    private static final int DEFAULT_OFFSET_DAY = 30;

    public String create(String audience) {
        return create(audience, DEFAULT_OFFSET_DAY);
    }

    /**
     * jwt token创建函数
     * @param audience 用户信息
     * @param offsetDay 偏移天数
     * @return JwtToken
     */
    public String create(String audience, int offsetDay) {
        return JWT.create()
                .withExpiresAt(ZonedDateTime.now().plusDays(offsetDay).toInstant())
                .withAudience(audience)
                .sign(Algorithm.HMAC256(privateKey));
    }

    /**
     * jwt token解析函数
     * @param token jwt tokenCorsFilter
     * @return 如果为null, 则jwt token是错误的或者空值
     */
    public String parse(String token) {
        DecodedJWT decodedJWT;
        try {
            decodedJWT = JWT.require(Algorithm.HMAC256(privateKey)).build().verify(token);
        } catch (Exception ex) {
            return null;
        }
        return decodedJWT.getAudience().get(0);

    }
}
