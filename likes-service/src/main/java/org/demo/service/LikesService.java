package org.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.RedisClient;
import org.demo.RedisConstant;
import org.demo.helper.RedisHSetHelper;
import org.demo.mapper.LikesMapper;
import org.demo.pojo.Likes;
import org.demo.vo.Result;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikesService {

    private final LikesMapper likesMapper;
    private final RedisHSetHelper redisHSetHelper;
    private final RedisClient redisClient;

    private static final String CACHE_LIKES_ID = RedisConstant.CACHE_LIKES_ID;
    private static final String PERSIST_LIKES_ID_NUMS = RedisConstant.PERSIST_LIKES_ID_NUMS;

    private static final Long HALF_HOUR = 1800000L; //半小时刷一次

    private static final Long testRate = 1000L;
    public Result<Void> like(Long userId, Long videoId) {

        // 缓存点赞信息
        redisHSetHelper.addMember(CACHE_LIKES_ID + videoId, String.valueOf(userId));
        // 视频点赞记录+1
        redisClient.increase(PERSIST_LIKES_ID_NUMS + CACHE_LIKES_ID);
        return Result.success();
    }

    /*
    * 采用单条插入，避免SQL过长
    * */
    @Scheduled(initialDelay = 1800000L, fixedRate = 1800000L)
    public void regularFlushLikesCacheIntoDatabase() {

        Set<String> keys = redisHSetHelper.getKeys(CACHE_LIKES_ID + "*");
        /*
        * 避免并发添加扩容破坏map结构
        * */
        keys.parallelStream().forEach((key) -> {
            key = key.substring(key.lastIndexOf(":") + 1);
            /*
            * 避免并发添加破坏set结构
            * */
            String finalKey = key;
            redisHSetHelper.clear(CACHE_LIKES_ID + key, (userId) -> {
                Likes likes = new Likes();
                likes.setVideoId(Long.valueOf(finalKey));
                likes.setUserId(Long.valueOf((String) userId[0]));
                likesMapper.insert(likes);
            });
        });
    }

    public Result<Void> unlike(Long userId, Long videoId) {
        return null;
    }

    public Result<Void> isLike(Long userId, Long videoId) {
        return null;
    }

    public Result<Void> oneVideoLikesNums(Long videoId) {
        return null;
    }

    public Result<Void> userVideoLikesNums(Long userId) {
        return null;
    }

    public Result<Void> seriesVideoNums(Long seriesId) {
        return null;
    }
}
