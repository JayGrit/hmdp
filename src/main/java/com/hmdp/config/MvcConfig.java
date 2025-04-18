package com.hmdp.config;

import com.hmdp.controller.Interceptors.InfoInterceptor;
import com.hmdp.controller.Interceptors.LoginInterceptor;
import com.hmdp.controller.Interceptors.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;


@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

//        registry.addInterceptor(new InfoInterceptor());

        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate));

        registry.addInterceptor(new LoginInterceptor()).
                excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/blog/hot",
                        "/shop/**",
                        "/voucher/**",
                        "/shop-type/**",
                        "/upload/**"
                        );
    }
}
