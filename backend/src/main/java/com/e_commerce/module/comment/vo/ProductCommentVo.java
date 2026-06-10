package com.e_commerce.module.comment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductCommentVo {
    private Long id;
    private Long productId;
    private Long userId;
    private Long orderItemId;
    private String content;
    private Integer score;
    private String scoreText;
    private Integer status;
    private String statusText;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
    // 用户信息
    private String username;
    private String avatar;
    // 商品信息
    private String productName;
    private String productPic;
    // 回复列表
    private List<CommentReplyVo> replies;
}
