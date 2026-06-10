package com.e_commerce.module.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_comment")
public class ProductComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull(message = "商品id不能为空")
    private Long productId;
    @NotNull(message = "用户id不能为空")
    private Long userId;
    @NotNull(message = "订单项id不能为空")
    private Long orderItemId;
    @NotBlank(message = "评论内容不能为空")
    private String content;
    @NotNull(message = "评分不能为空")
    private Integer score;
    private Integer status;
    private LocalDateTime createTime;
}