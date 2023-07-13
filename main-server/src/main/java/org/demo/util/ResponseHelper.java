package org.demo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 非Controller返回Json对象工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseHelper {

    private final ObjectMapper objectMapper;

    public <T> void writeObject(HttpServletResponse response, T o) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()){
            if (o.getClass() != String.class) {
                writer.write(objectMapper.writeValueAsString(o));
            } else {
                writer.write(o.toString());
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
