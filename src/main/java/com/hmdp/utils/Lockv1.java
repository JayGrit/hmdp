package com.hmdp.utils;


import cn.hutool.core.lang.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class Lockv1 implements ILock {

    String name;
    String LOCK_PREFIX = "lock:";
    StringRedisTemplate stringRedisTemplate;
    String uuid = UUID.randomUUID().toString(true);

    public Lockv1(String name,StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock() {
        String key = LOCK_PREFIX + name;
        String value = uuid + Thread.currentThread().getId();
        Boolean success = this.stringRedisTemplate.opsForValue().setIfAbsent(key,value,5, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void delLock() {
        String myValue = uuid + Thread.currentThread().getId();
        String key = LOCK_PREFIX + name;
        String value = this.stringRedisTemplate.opsForValue().get(key);
        if(myValue.equals(value))
            stringRedisTemplate.delete(key);
    }
}
