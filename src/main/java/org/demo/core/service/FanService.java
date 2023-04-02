package org.demo.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.demo.core.controller.vo.InterestVo;
import org.demo.core.util.JsonBean;

public interface FanService {

    JsonBean<Void> interest(Long id);

    JsonBean<Void> notInterest(Long id);

    JsonBean<Page<InterestVo>> getInterestList(Integer pageNum, Integer pageSize);
}
