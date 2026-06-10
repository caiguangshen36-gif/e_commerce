package com.e_commerce.module.operlog.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.operlog.service.SysOperLogService;
import com.e_commerce.module.system.entity.SysOperLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sys/oper-log")
public class SysOperLogController {

    @Autowired
    private SysOperLogService sysOperLogService;

    @PostMapping("/list")
    public Result<PageVo<SysOperLog>> getListByCondition(@RequestBody Map<String, Object> params) {
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;

        Long userId = null;
        Object userIdObj = params.get("userId");

        // 安全地处理 userId 转换
        if (userIdObj != null) {
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();
            } else {
                // 如果是字符串，先转字符串再转 Long
                try {
                    userId = Long.valueOf(userIdObj.toString());
                } catch (NumberFormatException e) {
                    // 处理转换失败的情况，或者保持 null
                    userId = null;
                }
            }
        }

        String operation = (String) params.get("operation");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");

        PageVo<SysOperLog> page = sysOperLogService.getList(pageNum, pageSize, userId, operation, startTime, endTime);
        return Result.success(page);
    }
}
