package com.e_commerce.module.operlog.aspect;

import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.operlog.annotation.OperLog;
import com.e_commerce.module.operlog.service.SysOperLogService;
import com.e_commerce.module.system.entity.SysOperLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class SysOperLogAspect {

    @Autowired
    private SysOperLogService sysOperLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(com.e_commerce.module.operlog.annotation.OperLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperLog operLog = method.getAnnotation(OperLog.class);

        String operation = operLog.operation();
        if (operation == null || operation.isEmpty()) {
            operation = operLog.value();
        }

        String className = point.getTarget().getClass().getName();
        String methodName = className + "." + method.getName();

        String params = "{}";
        try {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                Map<String, Object> paramMap = new HashMap<>();
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg != null && !isSimpleType(arg)) {
                        paramMap.put("param" + i, arg);
                    } else if (arg != null) {
                        paramMap.put("param" + i, arg.toString());
                    }
                }
                params = objectMapper.writeValueAsString(paramMap);
            }
        } catch (Exception e) {
            log.warn("获取请求参数失败: {}", e.getMessage());
        }

        Long userId = null;
        try {
            userId = ThreadLocalUtil.getUserId();
        } catch (Exception e) {
            log.warn("获取当前用户ID失败: {}", e.getMessage());
        }

        Object result = null;
        boolean success = true;
        String errorMsg = null;

        try {
            result = point.proceed();
            return result;
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;

            try {
                SysOperLog sysLog = new SysOperLog();
                sysLog.setUserId(userId);
                sysLog.setOperation(operation);
                sysLog.setMethod(methodName);
                sysLog.setParams(params);
                sysLog.setCreateTime(java.time.LocalDateTime.now());

                sysOperLogService.saveLog(sysLog);
                log.info("操作日志记录成功: 用户={}, 操作={}, 方法={}, 耗时={}ms, 成功={}",
                        userId, operation, methodName, costTime, success);
            } catch (Exception e) {
                log.error("操作日志记录失败: {}", e.getMessage());
            }
        }
    }

    private boolean isSimpleType(Object obj) {
        return obj instanceof String ||
                obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Character ||
                obj instanceof HttpServletRequest;
    }
}
