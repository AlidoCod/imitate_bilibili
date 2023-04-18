package org.demo.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisHSetHelper {

    private final StringRedisTemplate stringRedisTemplate;

    public Set<String> getMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }
    // key userId username userId username

    public Boolean isMember(String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(key, value);
    }

    @SuppressWarnings("ConstantConditions")
    public Boolean addMember(String key, String value) {
        return stringRedisTemplate.opsForSet().add(key, value) == 1;
    }

    @SuppressWarnings("ConstantConditions")
    public Boolean removeMember(String key, String value) {
        return stringRedisTemplate.opsForSet().remove(key, value) == 1;
    }

    public Set<String> getCollectiveMembers(String key1, String key2) {
        return stringRedisTemplate.opsForSet().intersect(key1, key2);
    }

    /**
     * 获取差集，获取key1存在但key2不存在的元素
     * 所以如果想获取感兴趣的人，第一个key就得是别人，第二个key是自己
     * 别人关注了，但自己没关注；别人存在的元素，但自己不存在
     */
    public Set<String> getDifferentMembers(String key1, String key2) {
        return stringRedisTemplate.opsForSet().difference(key1, key2);
    }

    public Long getSize(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }
}
