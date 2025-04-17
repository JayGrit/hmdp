//package com.hmdp.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.util.TreeSet;
//
//@Configuration
//public class CorsConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")  // 匹配所有路径
//                .allowedOrigins("*") // 允许所有源（或指定具体源，如 "http://localhost:8080"）
//                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 允许的方法
//                .allowedHeaders("*")  // 允许所有请求头
//                .allowCredentials(true)  // 允许携带凭证
//                .maxAge(3600);  // 预检请求缓存时间
//    }
//}
