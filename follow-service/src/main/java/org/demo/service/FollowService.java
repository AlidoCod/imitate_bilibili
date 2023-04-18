package org.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.demo.RedisConstant;
import org.demo.helper.RedisHSetHelper;
import org.demo.mapper.RelationMapper;
import org.demo.mapper.UserMapper;
import org.demo.pojo.Relation;
import org.demo.pojo.User;
import org.demo.util.ObjectConverter;
import org.demo.vo.Result;
import org.demo.vo.UserVo;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 原本是想前面的100条关注采用redis存储，相当于缓存最新关注，后面的采用数据库存储，降低redis的负担
 * 但是考虑到，需要对用户是否已经关注进行检查，如果部分存在数据库中，就意味着用户每打开一次其他用户的界面，都要查询一次Redis和数据库，无疑效率是非常低的
 * 所以采用了redis全部存储数据，而数据库数据作为备份的方案。。。也就是正常情况下，完全不会使用数据库，但是为了防止redis宕机数据丢失，写一份数据到数据库保证数据安全性
 */
@Service
@RequiredArgsConstructor
public class FollowService {

    private final RedisHSetHelper redisHSetHelper;

    private final RelationMapper relationMapper;

    private final UserMapper userMapper;

    public Boolean isFollowed(Long userId, Long followId) {
        String key = RedisConstant.PERSIST_FOLLOW_ID + userId;
        return redisHSetHelper.isMember(key, String.valueOf(followId));
    }

    public void follow(Long userId, Long followId) {
        Boolean flag = redisHSetHelper.addMember(RedisConstant.PERSIST_FOLLOW_ID + userId, String.valueOf(followId));
        //user关注了follow
        redisHSetHelper.addMember(RedisConstant.PERSIST_FAN_ID + followId, String.valueOf(userId));
        //user是follow的粉丝
        // 因为是set所以不去判断原本是否有没关注也没关系
        if (flag) {
            Relation relation = new Relation();
            relation.setUserId(userId);
            relation.setFollowId(followId);
            relationMapper.insert(relation);
        }
        // 如果已经存在，则不插入数据库
    }

    public void unfollow(Long userId, Long followId) {
        Boolean flag = redisHSetHelper.removeMember(RedisConstant.PERSIST_FOLLOW_ID + userId, String.valueOf(followId));
        // 减少关注
        redisHSetHelper.removeMember(RedisConstant.PERSIST_FAN_ID + followId, String.valueOf(userId));
        // 减少粉丝
        if (flag) {
            relationMapper.deleteByMap(Map.of("user_id", userId, "follow_id", followId));
        }
    }

    /**
     *  获取我关注的人
     */
    public Page<UserVo> getFollowers(Page<UserVo> page, Long userId) {
        Set<String> members = redisHSetHelper.getMembers(RedisConstant.PERSIST_FOLLOW_ID + userId);
        return members.isEmpty() ? page : userMapper.selectUserVoPage(page, members);
    }

    /**
     *  获取我的粉丝
     */
    public Page<UserVo> getFans(Page<UserVo> page, Long userId) {
        Set<String> members = redisHSetHelper.getMembers(RedisConstant.PERSIST_FAN_ID + userId);
        return members.isEmpty() ? page : userMapper.selectUserVoPage(page, members);
    }

    /**
     *  获取共同的关注
     */
    public Page<UserVo> getCollectiveFollower(Page<UserVo> page, Long userIdA, Long userIdB) {
        Set<String> members = redisHSetHelper.getCollectiveMembers(RedisConstant.PERSIST_FOLLOW_ID + userIdA, RedisConstant.PERSIST_FOLLOW_ID + userIdB);
        return members.isEmpty() ? page : userMapper.selectUserVoPage(page, members);
    }

    /**
     *  获取可能感兴趣的人, 最简单的随机推荐算法
     *  1. 先获取所有关注的对象
     *  2. 获取与所有关注的对象的差集
     *  3. 随机选择
     * @param userId 自己的ID
     */
    public List<UserVo> getInterestingFollower(Long userId, int num) throws JsonProcessingException {
        Set<String> followers = redisHSetHelper.getMembers(RedisConstant.PERSIST_FOLLOW_ID + userId);
        Set<Object> concurrentHashSet = Sets.newConcurrentHashSet();
        // 确保没有重复元素
        followers.parallelStream().forEach(other -> {
            Set<String> differentMembers = redisHSetHelper.getDifferentMembers(RedisConstant.PERSIST_FOLLOW_ID + other, RedisConstant.PERSIST_FOLLOW_ID + userId);
            concurrentHashSet.addAll(differentMembers);
        });
        List<Object> list = concurrentHashSet.stream().toList();
        List<UserVo> result = new ArrayList<>();
        // ArrayList，set无法通过下标访问
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < num; i++) {
            int size = list.size();
            if (size == 0)
                break;
            // 避免数组越界
            int index = random.nextInt(size);
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", list.get(index)));
            result.add(ObjectConverter.convert(user, UserVo.class));
            list.remove(index);
            // 删除已经入选的元素
        }
        return result;
    }

    public Result<Long> getFollowersNums(Long userId) {
        return Result.successByData(redisHSetHelper.getSize(RedisConstant.PERSIST_FOLLOW_ID + userId));
    }

    public Result<Long> getFansNums(Long userId) {
        return Result.successByData(redisHSetHelper.getSize(RedisConstant.PERSIST_FAN_ID + userId));
    }

}
