package org.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.demo.RedisConstant;
import org.demo.dto.SeriesCreateDto;
import org.demo.dto.SeriesUpdateDto;
import org.demo.helper.RedisHSetHelper;
import org.demo.mapper.FollowSeriesMapper;
import org.demo.mapper.SeriesMapper;
import org.demo.pojo.FollowSeries;
import org.demo.pojo.Series;
import org.demo.util.ObjectConverter;
import org.demo.util.ThreadHolder;
import org.demo.vo.Result;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service(value = "seriesService")
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesMapper seriesMapper;
    private final FollowSeriesMapper followSeriesMapper;
    private final RedisHSetHelper redisHSetHelper;
    private final ApplicationContext context;

    public Result<Void> create(SeriesCreateDto dto) throws JsonProcessingException {

        Series series = ObjectConverter.convert(dto, Series.class);
        series.setUserId(ThreadHolder.getUser().getId());

        boolean flag = seriesMapper.insert(series) == 1;
        if (flag) {
            //自己注入自己，避免事务失效
            SeriesService service = context.getBean("seriesService", SeriesService.class);
            service.follow(ThreadHolder.getUser().getId(), series.getId());
        }

        return flag ? Result.success(String.valueOf(series.getId())) : Result.fail();
    }

    public Result<List<Series>> query(Long id) {
        List<Series> list = seriesMapper.selectByMap(Map.of("user_id", id));
        System.out.println(list);
        return Result.successByData(list);
    }

    public Result<Void> delete(Long id) {

        Series series = seriesMapper.selectById(id);
        if (series == null) {
            return Result.success("此id对应的合集不存在");
        }
        if (!Objects.equals(series.getUserId(), ThreadHolder.getUser().getId())) {
            return Result.success("你没有权限删除非自己创建的合集");
        }
        return seriesMapper.deleteById(id) == 1 ? Result.success() : Result.fail();
    }

    public Result<Void> update(SeriesUpdateDto dto) throws JsonProcessingException {

        Series series = seriesMapper.selectById(dto.getId());
        if (series == null) {
            return Result.success("此id对应的合集不存在");
        }
        if (!Objects.equals(series.getUserId(), ThreadHolder.getUser().getId())) {
            return Result.success("你没有权限更新非自己创建的合集");
        }
        Series convert = ObjectConverter.convert(dto, Series.class);
        return seriesMapper.updateById(convert) == 1 ? Result.success() : Result.fail();
    }

    @Transactional
    public Result<Void> follow(Long id, Long seriesId) {
        FollowSeries followSeries = new FollowSeries();
        followSeries.setSeriesId(seriesId);
        followSeries.setUserId(id);
        followSeriesMapper.insert(followSeries);
        redisHSetHelper.addMember(RedisConstant.PERSIST_SERIES_ID + seriesId, String.valueOf(id));
        return Result.success();
    }

    @Transactional
    public Result<Void> unfollow(Long id, Long seriesId) {
        followSeriesMapper.deleteByMap(Map.of("series_id", seriesId, "user_id", id));
        redisHSetHelper.removeMember(RedisConstant.PERSIST_SERIES_ID + seriesId, String.valueOf(id));
        return Result.success();
    }

    public Result<Long> followersNums(Long id) {
        return Result.successByData(redisHSetHelper.getSize(RedisConstant.PERSIST_SERIES_ID + id));
    }
}
