package com.e_commerce.module.notice.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.notice.dto.SysNoticeDto;
import com.e_commerce.module.notice.service.SysNoticeService;
import com.e_commerce.module.notice.vo.SysNoticeTypeVo;
import com.e_commerce.module.notice.vo.SysNoticeVo;
import com.e_commerce.module.notice.vo.UmsUserNoticeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 系统通知控制器
 * 提供系统通知相关的API接口，包括管理员端和用户端的通知管理功能
 */
@RestController
@RequestMapping("/notice")
public class SysNoticeController {

    @Autowired
    private SysNoticeService noticeService;  // 注入通知服务

    /**
     * 获取通知类型列表
     * @return 返回所有通知类型列表
     */
    @GetMapping("/admin/types")
    public Result<List<SysNoticeTypeVo>> getNoticeTypes() {
        List<SysNoticeTypeVo> list = noticeService.getNoticeTypes();
        return Result.success(list);
    }

    @GetMapping("/user/types")
    public Result<List<SysNoticeTypeVo>> getNoticeUserTypes() {
        List<SysNoticeTypeVo> list = noticeService.getNoticeUserTypes();
        return Result.success(list);
    }

    /**
     * 获取通知列表
     * @param params 查询参数，包含通知类型、标题、是否已读等
     * @return 返回通知列表
     */
    @PostMapping("/admin/list")
    public Result<PageVo<SysNoticeVo>> getNoticeList(@RequestBody Map<String, Object> params) {
        Long adminId = ThreadLocalUtil.getUserId();  // 获取当前管理员ID
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        // 处理查询参数
        Integer noticeType = params.get("noticeType") != null ? ((Number) params.get("noticeType")).intValue() : null;
        String title = (String) params.get("title");
        Integer isRead = params.get("isRead") != null ? ((Number) params.get("isRead")).intValue() : null;
        PageVo<SysNoticeVo> page = noticeService.getNoticeList(pageNum, pageSize, adminId, noticeType, title, isRead);
        return Result.success(page);
    }

    /**
     * 获取通知详情
     * @param params 包含通知ID的参数
     * @return 返回通知详情
     */
    @PostMapping("/admin/detail")
    public Result<SysNoticeVo> getNoticeDetail(@RequestBody Map<String, Long> params) {
        Long adminId = ThreadLocalUtil.getUserId();
        Long noticeId = params.get("noticeId");
        SysNoticeVo vo = noticeService.getNoticeDetail(adminId, noticeId);
        if (vo != null) {
            return Result.success(vo);
        } else {
            return Result.error("消息不存在");
        }
    }

    /**
     * 获取未读通知数量
     * @return 返回未读通知数量
     */
    @GetMapping("/admin/unread-count")
    public Result<Map<String, Integer>> getUnreadCount() {
        Long adminId = ThreadLocalUtil.getUserId();
        int count = noticeService.getUnreadCount(adminId);
        return Result.success(Map.of("unreadCount", count));
    }

    /**
     * 标记通知为已读
     * @param params 包含通知ID列表的参数
     * @return 返回标记已读的数量
     */
    @PostMapping("/admin/mark-read")
    public Result<Integer> markAsRead(@RequestBody Map<String, Object> params) {
        Long adminId = ThreadLocalUtil.getUserId();
        List<?> rawIds = (List<?>) params.get("ids");

        // 如果没有提供通知ID，则标记所有通知为已读
        if (rawIds == null || rawIds.isEmpty()) {
            int count = noticeService.markAllAsRead(adminId);
            return Result.success(count);
        }

        // 处理通知ID列表
        List<Long> noticeIds = new ArrayList<>();
        for (int i = 0; i < rawIds.size(); i++) {
            Object obj = rawIds.get(i);
            if (obj instanceof String) {
                noticeIds.add(Long.parseLong((String) obj));
            } else if (obj instanceof Number) {
                noticeIds.add(((Number) obj).longValue());
            } else {
                throw new IllegalArgumentException("无效的通知ID类型");
            }
        }

        int count = noticeService.markAsRead(adminId, noticeIds);
        return Result.success(count);
    }

    /**
     * 删除通知
     * @param params 包含通知ID的参数
     * @return 返回操作结果
     */
    @PostMapping("/admin/delete")
    public Result<String> deleteNotice(@RequestBody Map<String, Long> params) {
        Long noticeId = params.get("noticeId");
        boolean success = noticeService.deleteNotice(noticeId);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 发送通知
     * @param dto 通知数据传输对象
     * @return 返回操作结果
     */
    @PostMapping("/admin/send")
    public Result<String> sendNotice(@RequestBody SysNoticeDto dto) {
        boolean success = noticeService.sendNotice(dto);
        if (success) {
            return Result.success("发送成功");
        } else {
            return Result.error("发送失败");
        }
    }

    /**
     * 向指定用户发送通知
     * @param params 包含用户ID、通知类型、标题、内容等参数
     * @return 返回操作结果
     */
    @PostMapping("/admin/send-to-user")
    public Result<String> sendNoticeToUser(@RequestBody Map<String, Object> params) {
        // 处理参数
        Long userId = params.get("userId") != null ? ((Number) params.get("userId")).longValue() : null;
        Integer noticeType = params.get("noticeType") != null ? ((Number) params.get("noticeType")).intValue() : null;
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String bizId = (String) params.get("bizId");

        if (userId == null) {
            return Result.error("用户ID不能为空");
        }

        boolean success = noticeService.sendUserNotice(userId, noticeType, title, content, bizId);
        if (success) {
            return Result.success("发送成功");
        } else {
            return Result.error("发送失败");
        }
    }

    // ============ 用户端接口 ============

    /**
     * 获取用户通知列表
     * @param params 查询参数，包含通知类型、是否已读等
     * @return 返回用户通知列表
     */
    @PostMapping("/user/list")
    public Result<PageVo<UmsUserNoticeVo>> getUserNoticeList(@RequestBody Map<String, Object> params) {
        Long userId = ThreadLocalUtil.getUserId();
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        // 处理查询参数
        Integer noticeType = params.get("noticeType") != null ? ((Number) params.get("noticeType")).intValue() : null;
        Integer isRead = params.get("isRead") != null ? ((Number) params.get("isRead")).intValue() : null;
        PageVo<UmsUserNoticeVo> page = noticeService.getUserNoticeList(pageNum, pageSize, userId, noticeType, isRead);
        return Result.success(page);
    }

    /**
     * 获取用户通知详情
     * @param params 包含通知ID的参数
     * @return 返回用户通知详情
     */
    @PostMapping("/user/detail")
    public Result<UmsUserNoticeVo> getUserNoticeDetail(@RequestBody Map<String, Long> params) {
        Long userId = ThreadLocalUtil.getUserId();
        Long noticeId = params.get("noticeId");
        UmsUserNoticeVo vo = noticeService.getUserNoticeDetail(userId, noticeId);
        if (vo != null) {
            return Result.success(vo);
        } else {
            return Result.error("消息不存在");
        }
    }

    /**
     * 获取用户未读通知数量
     * @return 返回用户未读通知数量
     */
    @GetMapping("/user/unread-count")
    public Result<Map<String, Integer>> getUserUnreadCount() {
        Long userId = ThreadLocalUtil.getUserId();
        int count = noticeService.getUserUnreadCount(userId);
        return Result.success(Map.of("unreadCount", count));
    }

    /**
     * 标记用户通知为已读
     * @param params 包含通知ID列表的参数
     * @return 返回标记已读的数量
     */
    @PostMapping("/user/mark-read")
    public Result<Integer> markUserNoticeRead(@RequestBody Map<String, Object> params) {
        Long userId = ThreadLocalUtil.getUserId();
        List<?> rawIds = (List<?>) params.get("ids");

        List<Long> ids = null;
        if (rawIds != null && !rawIds.isEmpty()) {
            ids = new ArrayList<>();
            for (int i = 0; i < rawIds.size(); i++) {
                Object obj = rawIds.get(i);
                if (obj instanceof String) {
                    ids.add(Long.parseLong((String) obj));
                } else if (obj instanceof Number) {
                    ids.add(((Number) obj).longValue());
                } else {
                    throw new IllegalArgumentException("无效的消息ID类型");
                }
            }
        }

        int count = noticeService.markUserNoticeRead(userId, ids);
        return Result.success(count);
    }

    /**
     * 删除用户通知
     * @param params 包含通知ID的参数
     * @return 返回删除的数量
     */
    @PostMapping("/user/delete")
    public Result<Integer> deleteUserNotice(@RequestBody Map<String, Long> params) {
        Long userId = ThreadLocalUtil.getUserId();
        Long noticeId = params.get("noticeId");
        int count = noticeService.deleteUserNotice(userId, noticeId);
        return Result.success(count);
    }

    /**
     * 发送用户通知
     * @param params 包含通知类型、标题、内容等参数
     * @return 返回操作结果
     */
    @PostMapping("/user/send")
    public Result<String> sendUserNotice(@RequestBody Map<String, Object> params) {
        Long userId = ThreadLocalUtil.getUserId();
        // 处理参数
        Integer noticeType = params.get("noticeType") != null ? ((Number) params.get("noticeType")).intValue() : null;
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String bizId = (String) params.get("bizId");
        boolean success = noticeService.sendUserNotice(userId, noticeType, title, content, bizId);
        if (success) {
            return Result.success("发送成功");
        } else {
            return Result.error("发送失败");
        }
    }
}