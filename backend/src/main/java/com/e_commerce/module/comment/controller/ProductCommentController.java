package com.e_commerce.module.comment.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.comment.dto.CommentReplyDto;
import com.e_commerce.module.comment.dto.ProductCommentDto;
import com.e_commerce.module.comment.service.ProductCommentService;
import com.e_commerce.module.comment.vo.CommentReplyVo;
import com.e_commerce.module.comment.vo.ProductCommentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class ProductCommentController {

    @Autowired
    private ProductCommentService productCommentService;

    // 新增评论
    @PostMapping("/add")
    public Result<String> addComment(@RequestBody ProductCommentDto commentDto) {
        Long userId = ThreadLocalUtil.getUserId();
        productCommentService.addComment(userId, commentDto);
        return Result.success("评论成功");
    }

    // 根据商品ID查询评论列表
    @GetMapping("/list")
    public Result<List<ProductCommentVo>> getCommentsByProductId(
            @RequestParam(required = false) Long productId
    ) {
        if (productId == null || productId <= 0) {
            return Result.success(Collections.emptyList());
        }
        List<ProductCommentVo> comments = productCommentService.getCommentsByProductId(productId);
        return Result.success(comments);
    }

    // 根据用户ID查询评论列表
    @GetMapping("/user")
    public Result<List<ProductCommentVo>> getCommentsByUserId() {
        Long userId = ThreadLocalUtil.getUserId();
        List<ProductCommentVo> comments = productCommentService.getCommentsByUserId(userId);
        return Result.success(comments);
    }

    // 根据评论ID查询评论详情
    @GetMapping("/detail/{id}")
    public Result<ProductCommentVo> getCommentById(@PathVariable Long id) {
        ProductCommentVo comment = productCommentService.getCommentById(id);
        return Result.success(comment);
    }

    // 更新评论状态（管理员接口）
    @PostMapping("/updateStatus")
    public Result<String> updateStatus(@RequestBody Map<String, Object> request) {
        Long id = Long.valueOf(request.get("id").toString());
        Integer status = (Integer) request.get("status");
        productCommentService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    // 删除评论
    @PostMapping("/delete")
    public Result<String> deleteComment(@RequestBody Map<String, Long> request) {
        Long id = request.get("id");
        productCommentService.deleteComment(id);
        return Result.success("删除成功");
    }

    // 新增评论回复
    @PostMapping("/reply/add")
    public Result<String> addReply(@RequestBody CommentReplyDto replyDto) {
        Long replyUserId = ThreadLocalUtil.getUserId();
        productCommentService.addReply(replyUserId, replyDto);
        return Result.success("回复成功");
    }

    // 根据评论ID查询回复列表
    @GetMapping("/reply/list/{commentId}")
    public Result<List<CommentReplyVo>> getRepliesByCommentId(@PathVariable Long commentId) {
        List<CommentReplyVo> replies = productCommentService.getRepliesByCommentId(commentId);
        return Result.success(replies);
    }

    // 删除评论回复
    @PostMapping("/reply/delete")
    public Result<String> deleteReply(@RequestBody Map<String, Long> request) {
        Long id = request.get("id");
        productCommentService.deleteReply(id);
        return Result.success("删除成功");
    }
}