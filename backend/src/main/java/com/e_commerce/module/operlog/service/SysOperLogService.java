package com.e_commerce.module.operlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.operlog.mapper.SysOperLogMapper;
import com.e_commerce.module.system.entity.SysOperLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysOperLogService {

    @Autowired
    private SysOperLogMapper sysOperLogMapper;

    public int saveLog(SysOperLog log) {
        return sysOperLogMapper.insert(log);
    }

    public PageVo<SysOperLog> getList(Long pageNum, Long pageSize, Long userId, String operation, String startTime, String endTime) {
        Page<SysOperLog> mpPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(SysOperLog::getUserId, userId);
        }
        if (operation != null && !operation.isEmpty()) {
            wrapper.like(SysOperLog::getOperation, operation);
        }
        if (startTime != null) {
            wrapper.ge(SysOperLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysOperLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(SysOperLog::getCreateTime);
        Page<SysOperLog> result = sysOperLogMapper.selectPage(mpPage, wrapper);

        PageVo<SysOperLog> pageVo = new PageVo<>();
        pageVo.setList(result.getRecords());
        pageVo.setTotal(result.getTotal());
        return pageVo;
    }

    public List<SysOperLog> getListByUserId(Long userId) {
        return sysOperLogMapper.selectList(new LambdaQueryWrapper<SysOperLog>()
                .eq(SysOperLog::getUserId, userId)
                .orderByDesc(SysOperLog::getCreateTime));
    }
}
