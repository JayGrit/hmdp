package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdGenerator {

    private final long BEGIN_TIMESTAMP = 1731283200L;
    private final int SERIAL_NUMBER = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String prefix){
        // 1. 时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        String ymd = now.format(DateTimeFormatter.ofPattern("yy:MM:dd"));
        // 2. 序列号
        long count = stringRedisTemplate.opsForValue().increment(prefix + "icr:" + ymd);

        return timestamp << SERIAL_NUMBER | count;

    }

}
