package org.demo.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.demo.dto.SeriesCreateDto;
import org.demo.pojo.base.Tag;
import org.demo.service.SeriesService;
import org.demo.vo.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class SeriesServiceTest {

    @Autowired
    SeriesService seriesService;

    private static String cache;

    @Test
    void create() throws JsonProcessingException {
        try {
            SeriesCreateDto seriesCreateDto = new SeriesCreateDto();
            seriesCreateDto.setDescription("哈哈哈哈，写单元测试真是太有趣拉");
            seriesCreateDto.setTags(List.of(Tag.动漫, Tag.治愈));
            seriesCreateDto.setTitle("不想干啦");
            seriesCreateDto.setImageId(1642178276140650498L);
            Result<Void> result = seriesService.create(seriesCreateDto);
            cache = result.getMessage();
            log.debug(result.getMessage());
        } catch (Exception e) {

        }
    }

    @Test
    void query() {
        try {
            seriesService.query(Long.parseLong(cache));
        } catch (Exception e) {

        }
    }

    @Test
    void delete() {
        try {
            seriesService.delete(Long.valueOf(cache));
        } catch (Exception e) {
        }
    }

    @Test
    void update() {
    }

    @Test
    void follow() {
    }

    @Test
    void unfollow() {
    }

    @Test
    void followersNums() {
    }
}