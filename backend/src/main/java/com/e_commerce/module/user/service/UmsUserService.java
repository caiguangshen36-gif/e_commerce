package com.e_commerce.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.common.utils.BCryptUtils;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.user.entity.UmsUser;
import com.e_commerce.module.user.mapper.UmsUserMapper;
import com.e_commerce.module.user.vo.UserRegisterStatsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UmsUserService {
    @Autowired
    private UmsUserMapper umsUserMapper;

    public void register(UmsUser umsUser) {
        String password = BCryptUtils.encode(umsUser.getPassword());
        umsUser.setPassword(password);
        umsUserMapper.insert(umsUser);
    }

    public UmsUser getInfo(String username) {
        return umsUserMapper.selectOne(new LambdaQueryWrapper<UmsUser>().eq(UmsUser::getUsername, username));
    }

    public void update(UmsUser umsUser) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Number idNum = (Number) map.get("id");
        Long id = idNum.longValue();
        umsUser.setId(id);
        umsUserMapper.update(null, new LambdaUpdateWrapper<UmsUser>()
                .eq(UmsUser::getId, id)
                .set(UmsUser::getPhone, umsUser.getPhone())
                .set(UmsUser::getAvatar, umsUser.getAvatar())
                .set(UmsUser::getCreateTime, LocalDateTime.now()));
    }

    public void updatePassword(Long id, String password) {
        String encode = BCryptUtils.encode(password);
        umsUserMapper.update(null, new LambdaUpdateWrapper<UmsUser>()
                .eq(UmsUser::getId, id)
                .set(UmsUser::getPassword, encode));
    }

    public void updatePasswordVersion(Long id, Integer version) {
        umsUserMapper.update(null, new LambdaUpdateWrapper<UmsUser>()
                .eq(UmsUser::getId, id)
                .set(UmsUser::getPasswordVersion, version));
    }

    public void updateBalance(Long id, BigDecimal amount) {
        umsUserMapper.update(null, new LambdaUpdateWrapper<UmsUser>()
                .eq(UmsUser::getId, id)
                .set(UmsUser::getBalance, amount));
    }

    public void updateAddBalance(Long id, BigDecimal amount) {
        umsUserMapper.update(null, new LambdaUpdateWrapper<UmsUser>()
                .eq(UmsUser::getId, id)
                .setSql("balance = balance + " + amount));
    }

    /**
     * 获取用户注册统计（今日对比昨日，本周对比上周）
     */
    public UserRegisterStatsVo getRegisterStats() {
        UserRegisterStatsVo stats = new UserRegisterStatsVo();

        // 今日注册数
        Long today = umsUserMapper.selectCount(new LambdaQueryWrapper<UmsUser>()
                .apply("DATE(create_time) = CURDATE()"));
        stats.setTodayCount(today != null ? today.intValue() : 0);

        // 昨日注册数
        Long yesterday = umsUserMapper.selectCount(new LambdaQueryWrapper<UmsUser>()
                .apply("DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)"));
        stats.setYesterdayCount(yesterday != null ? yesterday.intValue() : 0);

        // 较昨日对比
        if (yesterday != null && yesterday > 0) {
            stats.setYesterdayDiff(stats.getTodayCount() - yesterday.intValue());
            double rate = (stats.getTodayCount() - yesterday.intValue()) * 100.0 / yesterday;
            stats.setYesterdayRate(Math.round(rate * 100.0) / 100.0);
        } else {
            stats.setYesterdayDiff(stats.getTodayCount());
            stats.setYesterdayRate(stats.getTodayCount() > 0 ? 100.0 : 0);
        }

        // 本周注册数
        Long thisWeek = umsUserMapper.selectCount(new LambdaQueryWrapper<UmsUser>()
                .apply("YEARWEEK(DATE(create_time), 1) = YEARWEEK(CURDATE(), 1)"));
        stats.setThisWeekCount(thisWeek != null ? thisWeek.intValue() : 0);

        // 上周注册数
        Long lastWeek = umsUserMapper.selectCount(new LambdaQueryWrapper<UmsUser>()
                .apply("YEARWEEK(DATE(create_time), 1) = YEARWEEK(CURDATE(), 1) - 1"));
        stats.setLastWeekCount(lastWeek != null ? lastWeek.intValue() : 0);

        // 较上周对比
        if (lastWeek != null && lastWeek > 0) {
            stats.setLastWeekDiff(stats.getThisWeekCount() - lastWeek.intValue());
            double rate = (stats.getThisWeekCount() - lastWeek.intValue()) * 100.0 / lastWeek;
            stats.setLastWeekRate(Math.round(rate * 100.0) / 100.0);
        } else {
            stats.setLastWeekDiff(stats.getThisWeekCount());
            stats.setLastWeekRate(stats.getThisWeekCount() > 0 ? 100.0 : 0);
        }

        // 总用户数
        Long total = umsUserMapper.selectCount(null);
        stats.setTotalCount(total != null ? total.intValue() : 0);

        return stats;
    }
}
