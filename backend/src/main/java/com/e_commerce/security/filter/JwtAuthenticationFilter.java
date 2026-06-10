package com.e_commerce.security.filter;

import com.e_commerce.common.utils.JwtUtil;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.security.config.SecurityWhitePaths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {

        String requestUri = request.getRequestURI();
        log.info("进入JWT过滤器，请求路径：{}", request.getRequestURI());

        if (Arrays.asList(SecurityWhitePaths.WHITE_LIST).contains(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        token = token.substring(7);

        try {
            // 解析 token
            Map<String, Object> claims = JwtUtil.parseToken(token);
            System.out.println("解析成功："+claims);

            String username = (String) claims.get("username");
            Integer userId = (Integer) claims.get("id");
            Integer tokenPwdVersion = (Integer) claims.get("passwordVersion");

            // 校验 Redis token 是否存在
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

             //密码版本校验
            if (userId != null && tokenPwdVersion != null) {
                String redisVersionStr = stringRedisTemplate.opsForValue().get("user:passwordVersion:" + userId);

                if(redisVersionStr == null) {
                    log.warn("用户{}密码版本不存在于Redis", userId);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                int redisPwdVersion = Integer.parseInt(redisVersionStr);
                if (!tokenPwdVersion.equals(redisPwdVersion)) {
                    log.warn("密码版本不匹配 token={} redis={}", tokenPwdVersion, redisPwdVersion);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }

            // 给 Spring Security 授权
            UserDetails userDetails = User.withUsername(username)
                    .password("")
                    .authorities("ROLE_ADMIN", "ROLE_USER")
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 存入当前用户信息
            ThreadLocalUtil.set(claims);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } finally {
            ThreadLocalUtil.remove();
            SecurityContextHolder.clearContext();
        }
    }
}