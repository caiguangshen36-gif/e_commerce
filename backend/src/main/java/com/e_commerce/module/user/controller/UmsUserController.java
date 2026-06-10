package com.e_commerce.module.user.controller;

import com.aliyun.oss.common.utils.StringUtils;
import com.e_commerce.common.utils.BCryptUtils;
import com.e_commerce.common.utils.JwtUtil;
import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.operlog.annotation.OperLog;
import com.e_commerce.module.user.vo.UserVo;
import com.e_commerce.module.user.vo.UserRegisterStatsVo;
import com.e_commerce.module.user.dto.UserBalanceDto;
import com.e_commerce.module.user.dto.UserLoginDto;
import com.e_commerce.module.user.dto.UserRegisterDto;
import com.e_commerce.module.user.entity.UmsUser;
import com.e_commerce.module.user.service.UmsUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/ums/user")
public class UmsUserController {
    @Autowired
    private UmsUserService umsUserService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    public boolean checkCaptcha(String captchaId, String inputCode) {
        String key = CAPTCHA_KEY_PREFIX + captchaId;
        String realCode = stringRedisTemplate.opsForValue().get(key);
        if (realCode == null || !realCode.equalsIgnoreCase(inputCode)) {
            return false;
        }
        stringRedisTemplate.delete(key);
        return true;
    }

    @PostMapping("/getInfo")
    public Result<UserVo> getInfo(){
        Map<String,Object>claims= ThreadLocalUtil.get();
        String name = claims.get("username").toString();
        UmsUser info = umsUserService.getInfo(name);

        UserVo vo =new UserVo();
        BeanUtils.copyProperties(info,vo);
        return Result.success(vo);
    }

    @OperLog(operation = "用户注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Validated UserRegisterDto registerDto){
        String captchaId = registerDto.getCaptchaId();
        String captchaCode = registerDto.getCaptchaCode();
        boolean captchaValid = checkCaptcha(captchaId, captchaCode);
        if (!captchaValid) {
            return Result.error("验证码错误或已过期");
        }

        UmsUser umsUser = new UmsUser();
        umsUser.setUsername(registerDto.getUsername());
        umsUser.setPassword(registerDto.getPassword());
        umsUser.setPhone(registerDto.getPhone());

        UmsUser info = umsUserService.getInfo(registerDto.getUsername());
        if(info!=null){
            return Result.error("用户已存在");
        }

        umsUserService.register(umsUser);
        log.info("用户注册成功：{}", registerDto.getUsername());
        return Result.success("注册成功");
    }

    @OperLog(operation = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody  @Validated UserLoginDto loginDto){
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        String captchaId = loginDto.getCaptchaId();
        String captchaCode = loginDto.getCaptchaCode();
//        校验验证码
        boolean captchaValid = checkCaptcha(captchaId, captchaCode);
        if (!captchaValid) {
            return Result.error("验证码错误或已过期");
        }

        UmsUser info = umsUserService.getInfo(username);
        if(info==null) {
            return Result.error("用户不存在");
        }
        if (info.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }
        boolean matches = BCryptUtils.matches(password, info.getPassword());
        if(matches){
            Map<String,Object> claims = new HashMap<>();
            claims.put("username",username);
            claims.put("id",info.getId());
            claims.put("passwordVersion",info.getPasswordVersion());

            String token = JwtUtil.genToken(claims);
            try {
                ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
                stringStringValueOperations.set(token,token,1, TimeUnit.HOURS);
                stringStringValueOperations.set("user:passwordVersion:" + info.getId(),info.getPasswordVersion().toString(), 1, TimeUnit.HOURS);
                log.info("Token存入Redis成功: {}", token);
            } catch (Exception e) {
                log.error("Token存入Redis失败: {}", token, e);
            }
            log.info("用户登录成功：{}", username);
            return Result.success(token);
        }
        log.warn("用户登录失败，密码错误：{}", username);
        return Result.error("密码错误");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody UmsUser umsUser){
        umsUserService.update(umsUser);
        return Result.success("更新成功");
    }

    @PostMapping("/updateBalance")
    public Result<String> updateBalance(@RequestBody UserBalanceDto userBalanceDto){
        Long userId = ThreadLocalUtil.getUserId();
        umsUserService.updateBalance(userId,userBalanceDto.getBalance());
        return Result.success("充值成功");
    }

    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@RequestBody Map<String,String> claims, @RequestHeader("Authorization") String token) {
        String oldPassword = claims.get("oldPassword");
        String newPassword = claims.get("newPassword");
        String repPassword = claims.get("repPassword");
        if (StringUtils.isNullOrEmpty(oldPassword) || StringUtils.isNullOrEmpty(newPassword) || StringUtils.isNullOrEmpty(repPassword)) {
            return Result.error("密码不能为空");
        }
        if (!newPassword.equals(repPassword)) {
            return Result.error("两次密码不一致");
        }
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = map.get("username").toString();
        UmsUser info = umsUserService.getInfo(username);

        if(BCryptUtils.matches(oldPassword,info.getPassword())) {
            umsUserService.updatePassword(info.getId(), newPassword);
            int newVersion=info.getPasswordVersion()+1;
            umsUserService.updatePasswordVersion(info.getId(),newVersion);

            stringRedisTemplate.opsForValue().set("user:passwordVersion:" + info.getId(), String.valueOf(newVersion));
            return Result.success("修改成功");
        }
        return Result.error("密码错误");
    }

    /**
     * 获取用户注册统计（今日对比昨日，本周对比上周）
     */
    @GetMapping("/admin/registerStats")
    public Result<UserRegisterStatsVo> getRegisterStats() {
        return Result.success(umsUserService.getRegisterStats());
    }
}