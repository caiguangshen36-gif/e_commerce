package com.e_commerce.module.notice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_admin_notice_read")
public class SysAdminNoticeRead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId;
    private Long noticeId;
    private LocalDateTime readTime;
}