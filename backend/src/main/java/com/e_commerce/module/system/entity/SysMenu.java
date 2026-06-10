package com.e_commerce.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotEmpty(message = "菜单名称不能为空")
    private String menuName;
    private Long parentId;
    private String path;
    private String icon;
    private Integer sort;
    @NotNull(message = "菜单类型不能为空")
    private Integer type;
}
