package com.e_commerce.module.notice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_notice_role")
public class SysNoticeRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noticeId;
    private Long roleId;
}