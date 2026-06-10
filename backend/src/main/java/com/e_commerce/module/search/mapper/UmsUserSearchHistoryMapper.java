package com.e_commerce.module.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.search.entity.UmsUserSearchHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UmsUserSearchHistoryMapper extends BaseMapper<UmsUserSearchHistory> {

    List<String> getUserHistoryKeywords(Long userId);
}
