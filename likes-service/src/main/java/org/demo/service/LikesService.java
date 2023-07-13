package org.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.RedisClient;
import org.demo.RedisConstant;
import org.demo.helper.RedisHSetHelper;
import org.demo.mapper.LikesMapper;
import org.demo.mapper.VideoMapper;
import org.demo.pojo.Likes;
import org.demo.pojo.Video;
import org.demo.vo.Result;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikesService {

    private final LikesMapper likesMapper;
    private final RedisHSetHelper redisHSetHelper;
    private final RedisClient redisClient;
    private final VideoMapper videoMapper;

    private static final String CACHE_LIKES_ID = RedisConstant.CACHE_LIKES_ID;
    private static final String PERSIST_LIKES_ID_NUMS = RedisConstant.PERSIST_LIKES_ID_NUMS;

    //半小时刷一次
    private static final Long HALF_HOUR = 1800000L;

    private static final Long TEST_RATE = 1000L;
    public Result<Void> like(Long userId, Long videoId) {

        // 缓存点赞信息
        redisHSetHelper.addMember(CACHE_LIKES_ID + videoId, String.valueOf(userId));
        // 视频点赞记录+1
        redisClient.increase(PERSIST_LIKES_ID_NUMS + videoId);
        return Result.ok();
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

    /*
    * 如果redis存在数据，就删除redis的直接返回，否则就删除数据库的
    * */
    public Result<Void> unlike(Long userId, Long videoId) {
        boolean flag = false;
        if (redisHSetHelper.removeMember(CACHE_LIKES_ID + videoId, String.valueOf(userId))) {
            flag = true;
        }
        if (!flag && likesMapper.deleteByMap(Map.of("user_id", userId, "video_id", videoId)) == 1) {
            flag = true;
        }
        if (flag) {
            redisClient.decrease(PERSIST_LIKES_ID_NUMS + videoId);
        }
        return Result.ok();
    }

    /*
    * 先查redis再查数据库
    * */
    public Result<Void> isLike(Long userId, Long videoId) {
        if (redisHSetHelper.isMember(CACHE_LIKES_ID + videoId, String.valueOf(userId))) {
            return Result.yes();
        }
        if (!likesMapper.selectByMap(Map.of("user_id", userId, "video_id", videoId)).isEmpty()) {
            return Result.yes();
        }
        return Result.no();
    }

    public Result<Void> oneVideoLikesNums(Long videoId) throws JsonProcessingException {
        return Result.success(redisClient.get(PERSIST_LIKES_ID_NUMS + videoId, String.class));
    }

    public Result<Void> userVideoLikesNums(Long userId) {
        List<Video> list = videoMapper.selectByMap(Map.of("user_id", userId));
        return countLikes(list);
    }

    public Result<Void> seriesVideoNums(Long seriesId) {
        List<Video> list = videoMapper.selectByMap(Map.of("series_id", seriesId));
        return countLikes(list);
    }

    @NotNull
    private Result<Void> countLikes(List<Video> list) {
        AtomicInteger count = new AtomicInteger();
        list.parallelStream().forEach(video -> {
            try {
                count.addAndGet(redisClient.get(PERSIST_LIKES_ID_NUMS + video.getId(), Integer.class));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        return Result.success(count.toString());
    }
}
