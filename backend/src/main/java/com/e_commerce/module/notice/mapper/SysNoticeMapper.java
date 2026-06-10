package com.e_commerce.module.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.notice.entity.SysNotice;
import com.e_commerce.module.notice.vo.SysNoticeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    @Select("<script>" +
            "SELECT n.id, n.notice_type, n.title, n.content, n.biz_id, n.create_time, " +
            "t.type_name as notice_type_name, " +
            "CASE WHEN r.id IS NULL THEN 0 ELSE 1 END as is_read, " +
            "r.read_time " +
            "FROM sys_notice n " +
            "INNER JOIN sys_notice_role nr ON n.id = nr.notice_id " +
            "INNER JOIN sys_user_role ur ON nr.role_id = ur.role_id " +
            "INNER JOIN sys_notice_type t ON n.notice_type = t.id " +
            "LEFT JOIN sys_admin_notice_read r ON n.id = r.notice_id AND r.admin_id = #{adminId} " +
            "WHERE ur.user_id = #{adminId} " +
            "<if test='noticeType != null'>AND n.notice_type = #{noticeType}</if> " +
            "<if test='title != null and title != \"\"'>AND n.title LIKE CONCAT('%', #{title}, '%')</if> " +
            "<if test='isRead != null'>AND CASE WHEN r.id IS NULL THEN 0 ELSE 1 END = #{isRead}</if> " +
            "ORDER BY n.create_time DESC" +
            "</script>")
    List<SysNoticeVo> selectNoticeList(@Param("adminId") Long adminId,
                                        @Param("noticeType") Integer noticeType,
                                        @Param("title") String title,
                                        @Param("isRead") Integer isRead);

    @Select("SELECT COUNT(*) FROM sys_notice n " +
            "INNER JOIN sys_notice_role nr ON n.id = nr.notice_id " +
            "INNER JOIN sys_user_role ur ON nr.role_id = ur.role_id " +
            "LEFT JOIN sys_admin_notice_read r ON n.id = r.notice_id AND r.admin_id = #{adminId} " +
            "WHERE ur.user_id = #{adminId} AND r.id IS NULL")
    int countUnread(Long adminId);
}