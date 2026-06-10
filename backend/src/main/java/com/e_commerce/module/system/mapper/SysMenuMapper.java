package com.e_commerce.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    List<SysMenu> getMenusByUserId(Long userId);

    List<SysMenu> getCurrentUserMenus(@Param("userId") Long userId);
}
