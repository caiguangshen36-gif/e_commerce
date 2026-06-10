package com.e_commerce.module.notice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_notice_type")
public class SysNoticeType {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String typeName;
    private String module;
}