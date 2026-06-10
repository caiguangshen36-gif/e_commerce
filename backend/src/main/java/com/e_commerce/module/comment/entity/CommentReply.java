package com.e_commerce.module.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment_reply")
public class CommentReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long commentId;
    @NotBlank
    private String replyContent;
    @NotNull
    private Integer replyType;
    @NotNull
    private Long replyUserId;
    private LocalDateTime createTime;
}