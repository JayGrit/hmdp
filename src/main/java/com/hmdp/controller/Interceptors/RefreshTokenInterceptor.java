package com.hmdp.controller.Interceptors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 通过前端传来的 token 拼接出 redis 中存储的 tokenKey
        String token = request.getHeader("authorization");
        String tokenKey = LOGIN_USER_KEY + token;

        // 读 redis
        // 放行，交由下一级拦截器做处理
        if (StrUtil.isBlank(token))
            return true;
        Map<Object,Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);
        // 刷新有效期
        stringRedisTemplate.expire(tokenKey ,LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 转化为 UserDTO 对象，保存至 ThreadLocal
        // 放行，交由下一级拦截器做处理
        if (userMap.isEmpty())
            return true;
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        UserHolder.saveUser(userDTO);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
