package com.e_commerce.module.notice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.notice.dto.SysNoticeDto;
import com.e_commerce.module.notice.entity.SysAdminNoticeRead;
import com.e_commerce.module.notice.entity.SysNotice;
import com.e_commerce.module.notice.entity.SysNoticeRole;
import com.e_commerce.module.notice.entity.SysNoticeType;
import com.e_commerce.module.notice.entity.UmsUserNotice;
import com.e_commerce.module.notice.mapper.SysAdminNoticeReadMapper;
import com.e_commerce.module.notice.mapper.SysNoticeMapper;
import com.e_commerce.module.notice.mapper.SysNoticeRoleMapper;
import com.e_commerce.module.notice.mapper.SysNoticeTypeMapper;
import com.e_commerce.module.notice.mapper.UmsUserNoticeMapper;
import com.e_commerce.module.notice.vo.SysNoticeTypeVo;
import com.e_commerce.module.notice.vo.SysNoticeVo;
import com.e_commerce.module.notice.vo.UmsUserNoticeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysNoticeService {

    @Autowired
    private SysNoticeMapper noticeMapper;

    @Autowired
    private SysNoticeRoleMapper noticeRoleMapper;

    @Autowired
    private SysAdminNoticeReadMapper noticeReadMapper;

    @Autowired
    private SysNoticeTypeMapper noticeTypeMapper;

    @Autowired
    private UmsUserNoticeMapper userNoticeMapper;

    public PageVo<SysNoticeVo> getNoticeList(Long pageNum, Long pageSize, Long adminId, Integer noticeType, String title, Integer isRead) {
        List<SysNoticeVo> fullList = noticeMapper.selectNoticeList(adminId, noticeType, title, isRead);
        for (int i = 0; i < fullList.size(); i++) {
            SysNoticeVo vo = fullList.get(i);
            if (vo.getIsRead() == 1) {
                vo.setIsReadText("已读");
            } else {
                vo.setIsReadText("未读");
            }
        }

        // 手动分页
        long total = fullList.size();
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize.intValue(), fullList.size());
        if (fromIndex >= fullList.size()) {
            fromIndex = 0;
            toIndex = 0;
        }
        List<SysNoticeVo> pageList = fullList.subList(fromIndex, toIndex);

        PageVo<SysNoticeVo> pageVo = new PageVo<>();
        pageVo.setList(pageList);
        pageVo.setTotal(total);
        return pageVo;
    }

    public SysNoticeVo getNoticeDetail(Long adminId, Long noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            return null;
        }
        SysNoticeVo vo = new SysNoticeVo();
        vo.setId(notice.getId());
        vo.setNoticeType(notice.getNoticeType());
        vo.setNoticeTypeName(getTypeNameById(notice.getNoticeType()));
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setBizId(notice.getBizId());
        vo.setCreateTime(notice.getCreateTime());

        SysAdminNoticeRead readRecord = noticeReadMapper.selectOne(
                new LambdaQueryWrapper<SysAdminNoticeRead>()
                        .eq(SysAdminNoticeRead::getAdminId, adminId)
                        .eq(SysAdminNoticeRead::getNoticeId, noticeId));
        if (readRecord != null) {
            vo.setIsRead(1);
            vo.setIsReadText("已读");
            vo.setReadTime(readRecord.getReadTime());
        } else {
            vo.setIsRead(0);
            vo.setIsReadText("未读");
        }
        return vo;
    }

    public int getUnreadCount(Long adminId) {
        return noticeMapper.countUnread(adminId);
    }

    @Transactional
    public int markAsRead(Long adminId, List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < noticeIds.size(); i++) {
            Long noticeId = noticeIds.get(i);
            SysAdminNoticeRead existing = noticeReadMapper.selectOne(
                    new LambdaQueryWrapper<SysAdminNoticeRead>()
                            .eq(SysAdminNoticeRead::getAdminId, adminId)
                            .eq(SysAdminNoticeRead::getNoticeId, noticeId));
            if (existing == null) {
                SysAdminNoticeRead read = new SysAdminNoticeRead();
                read.setAdminId(adminId);
                read.setNoticeId(noticeId);
                read.setReadTime(LocalDateTime.now());
                noticeReadMapper.insert(read);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public int markAllAsRead(Long adminId) {
        List<Long> readIds = noticeReadMapper.selectList(
                new LambdaQueryWrapper<SysAdminNoticeRead>()
                        .eq(SysAdminNoticeRead::getAdminId, adminId))
                .stream().map(SysAdminNoticeRead::getNoticeId).toList();

        noticeReadMapper.delete(
                new LambdaQueryWrapper<SysAdminNoticeRead>()
                        .eq(SysAdminNoticeRead::getAdminId, adminId));

        List<SysNoticeVo> allList = noticeMapper.selectNoticeList(adminId, null, null, null);
        List<Long> noticeIds = new ArrayList<>();
        for (int i = 0; i < allList.size(); i++) {
            noticeIds.add(allList.get(i).getId());
        }

        if (noticeIds.size() == 0) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < noticeIds.size(); i++) {
            Long noticeId = noticeIds.get(i);
            boolean isRead = false;
            for (int j = 0; j < readIds.size(); j++) {
                if (readIds.get(j).equals(noticeId)) {
                    isRead = true;
                    break;
                }
            }
            if (!isRead) {
                SysAdminNoticeRead read = new SysAdminNoticeRead();
                read.setAdminId(adminId);
                read.setNoticeId(noticeId);
                read.setReadTime(LocalDateTime.now());
                noticeReadMapper.insert(read);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public boolean deleteNotice(Long noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            return false;
        }
        noticeRoleMapper.delete(
                new LambdaQueryWrapper<SysNoticeRole>()
                        .eq(SysNoticeRole::getNoticeId, noticeId));
        return noticeMapper.deleteById(noticeId) > 0;
    }

    @Transactional
    public boolean sendNotice(SysNoticeDto dto) {
        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            throw new RuntimeException("请选择至少一个角色");
        }

        SysNotice notice = new SysNotice();
        notice.setNoticeType(dto.getNoticeType());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setBizId(dto.getBizId());

        int rows = noticeMapper.insert(notice);
        if (rows > 0) {
            for (Long roleId : dto.getRoleIds()) {
                SysNoticeRole nr = new SysNoticeRole();
                nr.setNoticeId(notice.getId());
                nr.setRoleId(roleId);
                noticeRoleMapper.insert(nr);
            }
            return true;
        }
        return false;
    }

    public List<SysNoticeTypeVo> getNoticeTypes() {
        return convertToTypeVoList(noticeTypeMapper.selectList(
                new LambdaQueryWrapper<SysNoticeType>().eq(SysNoticeType::getModule, "admin").orderByAsc(SysNoticeType::getId)));
    }

    // ============ 用户端消息方法 ============

    public List<SysNoticeTypeVo> getNoticeUserTypes() {
        return convertToTypeVoList(noticeTypeMapper.selectList(
                new LambdaQueryWrapper<SysNoticeType>().eq(SysNoticeType::getModule, "user").orderByAsc(SysNoticeType::getId)));
    }

    public PageVo<UmsUserNoticeVo> getUserNoticeList(Long pageNum, Long pageSize, Long userId, Integer noticeType, Integer isRead) {
        List<UmsUserNoticeVo> fullList = userNoticeMapper.selectUserNoticeList(userId, noticeType, isRead);
        for (int i = 0; i < fullList.size(); i++) {
            UmsUserNoticeVo vo = fullList.get(i);
            if (vo.getIsRead() == 1) {
                vo.setIsReadText("已读");
            } else {
                vo.setIsReadText("未读");
            }
        }

        // 手动分页
        long total = fullList.size();
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = Math.min(fromIndex + pageSize.intValue(), fullList.size());
        if (fromIndex >= fullList.size()) {
            fromIndex = 0;
            toIndex = 0;
        }
        List<UmsUserNoticeVo> pageList = fullList.subList(fromIndex, toIndex);

        PageVo<UmsUserNoticeVo> pageVo = new PageVo<>();
        pageVo.setList(pageList);
        pageVo.setTotal(total);
        return pageVo;
    }

    public UmsUserNoticeVo getUserNoticeDetail(Long userId, Long noticeId) {
        UmsUserNotice notice = userNoticeMapper.selectById(noticeId);
        if (notice == null || !notice.getUserId().equals(userId)) {
            return null;
        }
        UmsUserNoticeVo vo = new UmsUserNoticeVo();
        vo.setId(notice.getId());
        vo.setUserId(notice.getUserId());
        vo.setNoticeType(notice.getNoticeType());
        vo.setNoticeTypeName(getTypeNameById(notice.getNoticeType()));
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setBizId(notice.getBizId());
        vo.setIsRead(notice.getIsRead());
        vo.setIsReadText(notice.getIsRead() == 1 ? "已读" : "未读");
        vo.setReadTime(notice.getReadTime());
        vo.setCreateTime(notice.getCreateTime());
        return vo;
    }

    public int getUserUnreadCount(Long userId) {
        return userNoticeMapper.selectCount(
                new LambdaQueryWrapper<UmsUserNotice>()
                        .eq(UmsUserNotice::getUserId, userId)
                        .eq(UmsUserNotice::getIsRead, 0)).intValue();
    }

    @Transactional
    public int markUserNoticeRead(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            userNoticeMapper.update(null,
                    new LambdaUpdateWrapper<UmsUserNotice>()
                            .eq(UmsUserNotice::getUserId, userId)
                            .set(UmsUserNotice::getIsRead, 1)
                            .set(UmsUserNotice::getReadTime, LocalDateTime.now()));
            return userNoticeMapper.selectCount(
                    new LambdaQueryWrapper<UmsUserNotice>().eq(UmsUserNotice::getUserId, userId)).intValue();
        }
        return userNoticeMapper.update(null,
                new LambdaUpdateWrapper<UmsUserNotice>()
                        .eq(UmsUserNotice::getUserId, userId)
                        .in(UmsUserNotice::getId, ids)
                        .set(UmsUserNotice::getIsRead, 1)
                        .set(UmsUserNotice::getReadTime, LocalDateTime.now()));
    }

    @Transactional
    public int deleteUserNotice(Long userId, Long noticeId) {
        return userNoticeMapper.delete(
                new LambdaQueryWrapper<UmsUserNotice>()
                        .eq(UmsUserNotice::getId, noticeId)
                        .eq(UmsUserNotice::getUserId, userId));
    }

    @Transactional
    public boolean sendUserNotice(Long userId, Integer noticeType, String title, String content, String bizId) {
        UmsUserNotice notice = new UmsUserNotice();
        notice.setUserId(userId);
        notice.setNoticeType(noticeType);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setBizId(bizId);
        return userNoticeMapper.insert(notice) > 0;
    }

    // ============ 私有辅助方法 ============

    private String getTypeNameById(Integer typeId) {
        SysNoticeType type = noticeTypeMapper.selectById(typeId);
        return type != null ? type.getTypeName() : null;
    }

    private List<SysNoticeTypeVo> convertToTypeVoList(List<SysNoticeType> types) {
        List<SysNoticeTypeVo> voList = new ArrayList<>();
        for (SysNoticeType type : types) {
            SysNoticeTypeVo vo = new SysNoticeTypeVo();
            vo.setId(type.getId());
            vo.setTypeName(type.getTypeName());
            vo.setModule(type.getModule());
            voList.add(vo);
        }
        return voList;
    }
}