package com.e_commerce.module.comment.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.module.comment.dto.CommentReplyDto;
import com.e_commerce.module.comment.dto.ProductCommentDto;
import com.e_commerce.module.comment.entity.CommentReply;
import com.e_commerce.module.comment.entity.ProductComment;
import com.e_commerce.module.comment.mapper.CommentReplyMapper;
import com.e_commerce.module.comment.mapper.ProductCommentMapper;
import com.e_commerce.module.comment.vo.CommentReplyVo;
import com.e_commerce.module.comment.vo.ProductCommentVo;
import com.e_commerce.module.user.entity.UmsUser;
import com.e_commerce.module.user.mapper.UmsUserMapper;
import com.e_commerce.module.product.entity.PmsProduct;
import com.e_commerce.module.product.mapper.PmsProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductCommentService {

    @Autowired
    private ProductCommentMapper productCommentMapper;

    @Autowired
    private CommentReplyMapper commentReplyMapper;

    @Autowired
    private UmsUserMapper umsUserMapper;

    @Autowired
    private PmsProductMapper pmsProductMapper;

    // 新增评论
    public void addComment(Long userId, ProductCommentDto commentDto) {
        ProductComment comment = new ProductComment();
        comment.setProductId(commentDto.getProductId());
        comment.setUserId(userId);
        comment.setOrderItemId(commentDto.getOrderItemId());
        comment.setContent(commentDto.getContent());
        comment.setScore(commentDto.getScore());
        productCommentMapper.insert(comment);
    }

    // 根据商品ID查询评论列表
    public List<ProductCommentVo> getCommentsByProductId(Long productId) {
        List<ProductComment> comments = productCommentMapper.selectList(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getProductId, productId)
                        .eq(ProductComment::getStatus, 1)
                        .orderByDesc(ProductComment::getCreateTime));
        return convertToVoList(comments);
    }

    // 根据用户ID查询评论列表
    public List<ProductCommentVo> getCommentsByUserId(Long userId) {
        List<ProductComment> comments = productCommentMapper.selectList(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getUserId, userId)
                        .orderByDesc(ProductComment::getCreateTime));
        return convertToVoList(comments);
    }

    // 根据评论ID查询评论详情
    public ProductCommentVo getCommentById(Long id) {
        ProductComment comment = productCommentMapper.selectById(id);
        if (comment == null) {
            return null;
        }
        return convertToVo(comment);
    }

    // 更新评论状态
    public void updateStatus(Long id, Integer status) {
        productCommentMapper.update(null, new LambdaUpdateWrapper<ProductComment>()
                .eq(ProductComment::getId, id)
                .set(ProductComment::getStatus, status));
    }

    // 删除评论
    public void deleteComment(Long id) {
        productCommentMapper.deleteById(id);
    }

    // 新增评论回复
    public void addReply(Long replyUserId, CommentReplyDto replyDto) {
        CommentReply reply = new CommentReply();
        reply.setCommentId(replyDto.getCommentId());
        reply.setReplyContent(replyDto.getReplyContent());
        reply.setReplyType(replyDto.getReplyType());
        reply.setReplyUserId(replyUserId);
        commentReplyMapper.insert(reply);
    }

    // 根据评论ID查询回复列表
    public List<CommentReplyVo> getRepliesByCommentId(Long commentId) {
        List<CommentReply> replies = commentReplyMapper.selectList(
                new LambdaQueryWrapper<CommentReply>()
                        .eq(CommentReply::getCommentId, commentId)
                        .orderByAsc(CommentReply::getCreateTime));
        List<CommentReplyVo> replyVos = new ArrayList<>();
        for (CommentReply reply : replies) {
            CommentReplyVo vo = new CommentReplyVo();
            BeanUtil.copyProperties(reply, vo);
            // 设置回复类型文本
            if (reply.getReplyType() == 1) {
                vo.setReplyTypeText("用户回复");
            } else if (reply.getReplyType() == 2) {
                vo.setReplyTypeText("商家回复");
            }
            // 设置回复用户信息
            UmsUser user = umsUserMapper.selectById(reply.getReplyUserId());
            if (user != null) {
                vo.setReplyUsername(user.getUsername());
                vo.setReplyUserAvatar(user.getAvatar());
            }
            replyVos.add(vo);
        }
        return replyVos;
    }

    // 删除评论回复
    public void deleteReply(Long id) {
        commentReplyMapper.deleteById(id);
        commentReplyMapper.delete(new LambdaQueryWrapper<CommentReply>()
                .eq(CommentReply::getCommentId, id));
    }

    // 转换为VO列表
    private List<ProductCommentVo> convertToVoList(List<ProductComment> comments) {
        List<ProductCommentVo> commentVos = new ArrayList<>();
        for (ProductComment comment : comments) {
            commentVos.add(convertToVo(comment));
        }
        return commentVos;
    }

    // 转换为VO
    private ProductCommentVo convertToVo(ProductComment comment) {
        ProductCommentVo vo = new ProductCommentVo();
        BeanUtil.copyProperties(comment, vo);
        // 设置评分文本
        vo.setScoreText(comment.getScore() + "星");
        // 设置状态文本
        if (comment.getStatus() == 1) {
            vo.setStatusText("启用");
        } else {
            vo.setStatusText("禁用");
        }
        // 设置用户信息
        UmsUser user = umsUserMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setAvatar(user.getAvatar());
        }
        // 设置商品信息
        PmsProduct product = pmsProductMapper.selectById(comment.getProductId());
        if (product != null) {
            vo.setProductName(product.getProductName());
            vo.setProductPic(product.getPic());
        }
        // 设置回复列表
        List<CommentReplyVo> replies = getRepliesByCommentId(comment.getId());
        vo.setReplies(replies);
        return vo;
    }
}
