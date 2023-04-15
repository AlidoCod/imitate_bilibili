package org.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.demo.controller.vo.InterestVo;
import org.demo.controller.vo.JsonBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FanService {
    public JsonBean<Page<InterestVo>> getInterestList(Integer pageNum, Integer pageSize) {
        return null;
    }

    public JsonBean<Void> notInterest(Long id) {
        return null;
    }

    public JsonBean<Void> interest(Long id) {
        return null;
    }
}
