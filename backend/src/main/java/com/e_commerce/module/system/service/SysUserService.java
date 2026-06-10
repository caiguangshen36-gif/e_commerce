package com.e_commerce.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.common.utils.BCryptUtils;
import com.e_commerce.module.system.entity.SysUser;
import com.e_commerce.module.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    public SysUser selectByName(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    public List<SysUser> list() {
        return sysUserMapper.selectList(null);
    }

    public void add(String username, String password, String phone) {
        String encode = BCryptUtils.encode(password);
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(encode);
        user.setPhone(phone);
        user.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
    }

    public SysUser getInfo(Long id) {
        return sysUserMapper.selectById(id);
    }

    public void updatePassword(String password, Long id) {
        String encode = BCryptUtils.encode(password);
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getPassword, encode));
    }

    public void updateStatus(Long id, Integer status) {
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getStatus, status));
    }

    public void updatePasswordVersion(Long id, Integer passwordVersion) {
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getPasswordVersion, passwordVersion));
    }

    public void updateInfo(Long id, String username, String phone) {
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getUsername, username)
                .set(SysUser::getPhone, phone));
    }
}
