package org.demo.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 对象转换
     * @param src 源对象
     * @param clazz 目的对象的类对象
     * @param <T> 目的对象的类
     * @return  目的对象
     */
    public static <T> T convert(Object src, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(objectMapper.writeValueAsString(src), clazz);
    }
}
