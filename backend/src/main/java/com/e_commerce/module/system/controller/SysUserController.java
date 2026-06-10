package com.e_commerce.module.system.controller;

import com.aliyun.oss.common.utils.StringUtils;
import com.e_commerce.common.utils.*;
import com.e_commerce.module.operlog.annotation.OperLog;
import com.e_commerce.module.system.entity.SysMenu;
import com.e_commerce.module.system.service.SysMenuService;
import com.e_commerce.module.system.vo.SysUserVo;
import com.e_commerce.module.system.dto.SysUserLoginDto;
import com.e_commerce.module.system.dto.SysUserRegisterDto;
import com.e_commerce.module.system.entity.SysUser;
import com.e_commerce.module.system.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/sys/user")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    public boolean checkCaptcha(String captchaId, String inputCode) {
        String key = CAPTCHA_KEY_PREFIX + captchaId;
        String realCode = stringRedisTemplate.opsForValue().get(key);
        if (realCode == null || !realCode.equalsIgnoreCase(inputCode)) {
            return false;
        }
        // 校验通过，删除验证码（防止复用）
        stringRedisTemplate.delete(key);
        return true;
    }

    @OperLog(operation = "管理员登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody SysUserLoginDto loginDto) {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        String captchaId = loginDto.getCaptchaId();
        String captchaCode = loginDto.getCaptchaCode();

        // 校验验证码
        boolean captchaValid = checkCaptcha(captchaId, captchaCode);
        if (!captchaValid) {
            return Result.error("验证码错误或已过期");
        }

        SysUser sysUser = sysUserService.selectByName(username);
        if (sysUser == null) {
            return Result.error("用户不存在");
        }

        if (sysUser.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        if (!BCryptUtils.matches(password, sysUser.getPassword())) {
            log.warn("用户登录失败，密码错误：{}", username);
            return Result.error("密码错误");
        }

        // 从Redis拿最新版本号
        String redisVersion = stringRedisTemplate.opsForValue().get("user:passwordVersion:" + sysUser.getId());
        if (redisVersion != null) {
            int currentDbVersion = sysUser.getPasswordVersion();
            int currentRedisVersion = Integer.parseInt(redisVersion);

            // 如果数据库版本 < Redis版本 → 代表密码已被修改
            if (currentDbVersion < currentRedisVersion) {
                return Result.error("密码已修改，请使用新密码登录");
            }
        }
        List<SysMenu> currentUserMenus = sysMenuService.getCurrentUserMenus(sysUser.getId());

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("id", sysUser.getId());
        claims.put("passwordVersion", sysUser.getPasswordVersion());
        String token = JwtUtil.genToken(claims);

        try {
            ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
            stringStringValueOperations.set(token, token, 1, TimeUnit.HOURS);
            stringRedisTemplate.opsForValue().set(
                    "user:passwordVersion:" + sysUser.getId(),
                    sysUser.getPasswordVersion().toString(),
                    1, TimeUnit.HOURS
            );
            log.info("Token存入Redis成功: {}", token);
        } catch (Exception e) {
            log.error("Token存入Redis失败: {}", token, e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("menus", currentUserMenus);
        result.put("userId", sysUser.getId());
        result.put("username", username);

        log.info("用户登录成功：{}，生成的Token: {}", username, token);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody @Validated SysUserRegisterDto registerDto){
        String username = registerDto.getUsername();
        String password = registerDto.getPassword();
        String phone=registerDto.getPhone();
        SysUser sysUser = sysUserService.selectByName(username);
        if(sysUser!=null){
            return Result.error("用户已存在");
        }
        sysUserService.add(username,password,phone);
        log.info("用户注册成功：{}", username);
        return Result.success("添加成功");
    }

    @PostMapping("/info")
    public Result<SysUserVo> info(){
        Map<String, Object> claims = ThreadLocalUtil.get();
        String username = claims.get("username").toString();
        SysUser sysUser = sysUserService.selectByName(username);
        SysUserVo vo=new SysUserVo();
        BeanUtils.copyProperties(sysUser,vo);
        return Result.success(vo);
    }

    @PostMapping("/list")
    public Result<List<SysUserVo>> list() {
        List<SysUser> list = sysUserService.list();
        List<SysUserVo> voList= BeanConvertUtils.convertList(list,SysUserVo.class);
        return Result.success(voList);
    }

    @PostMapping("/updateStatus")
    public Result<String> updateStatus(@RequestBody Map<String, Integer> params) {
        Long userId = Long.valueOf(params.get("id"));
        Integer status = params.get("status"); // 0禁用 1启用

        if (userId == null || status == null) {
            return Result.error("参数不能为空");
        }

        sysUserService.updateStatus(userId,status);
        log.info("管理员账号状态修改成功，用户ID：{}，状态：{}", userId, status);
        return Result.success("状态修改成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? ((Number) params.get("id")).longValue() : null;
        String username = (String) params.get("username");
        String phone = (String) params.get("phone");

        if (id == null || StringUtils.isNullOrEmpty(username)) {
            return Result.error("ID和用户名不能为空");
        }

        sysUserService.updateInfo(id, username, phone);
        log.info("管理员信息修改成功，用户ID：{}，用户名：{}", id, username);
        return Result.success("修改成功");
    }

    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@RequestBody Map<String,String> claims, @RequestHeader("Authorization") String token){
        String oldPassword = claims.get("oldPassword");
        String newPassword = claims.get("newPassword");
        String repPassword = claims.get("repPassword");
        if (StringUtils.isNullOrEmpty(oldPassword) || StringUtils.isNullOrEmpty(newPassword) || StringUtils.isNullOrEmpty(repPassword)) {
            return Result.error("密码不能为空");
        }
        if (!newPassword.equals(repPassword)) {
            return Result.error("两次密码不一致");
        }
        Map<String ,Object>map=ThreadLocalUtil.get();
        String username = map.get("username").toString();
        SysUser sysUser = sysUserService.selectByName(username);
        log.info("用户修改密码：{}", username);
        System.out.println("用户修改密码："+ username);
        System.out.println(sysUser);
        if(BCryptUtils.matches(oldPassword,sysUser.getPassword())) {
            sysUserService.updatePassword(newPassword, sysUser.getId());
            int newVersion=sysUser.getPasswordVersion()+1;
            sysUserService.updatePasswordVersion(sysUser.getId(),newVersion);
//            ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
//            stringStringValueOperations.getOperations().delete(token);
            stringRedisTemplate.opsForValue().set("user:passwordVersion:" + sysUser.getId(), String.valueOf(newVersion));
            return Result.success("修改成功");
        }
        return Result.error("密码错误");
    }

}

