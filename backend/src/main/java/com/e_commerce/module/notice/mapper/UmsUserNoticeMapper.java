package com.e_commerce.module.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.e_commerce.module.notice.entity.UmsUserNotice;
import com.e_commerce.module.notice.vo.UmsUserNoticeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UmsUserNoticeMapper extends BaseMapper<UmsUserNotice> {

    @Select("<script>" +
            "SELECT un.id, un.user_id, un.notice_type, un.title, un.content, un.biz_id, " +
            "un.is_read, un.read_time, un.create_time, t.type_name as notice_type_name " +
            "FROM ums_user_notice un " +
            "LEFT JOIN sys_notice_type t ON un.notice_type = t.id " +
            "WHERE un.user_id = #{userId} " +
            "<if test='noticeType != null'>AND un.notice_type = #{noticeType}</if> " +
            "<if test='isRead != null'>AND un.is_read = #{isRead}</if> " +
            "ORDER BY un.create_time DESC" +
            "</script>")
    List<UmsUserNoticeVo> selectUserNoticeList(@Param("userId") Long userId,
                                                 @Param("noticeType") Integer noticeType,
                                                 @Param("isRead") Integer isRead);
}