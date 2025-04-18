package com.hmdp.utils;


import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class Lockv1 implements ILock {

    String name;
    String LOCK_PREFIX = "lock_";
    StringRedisTemplate stringRedisTemplate;

    public Lockv1(String name,StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(String name) {
        long threadId = Thread.currentThread().getId();
        String key = LOCK_PREFIX + name;
        String value = Long.toString(threadId);
        Boolean success = this.stringRedisTemplate.opsForValue().setIfAbsent(key,value,5, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void delLock(String lockName) {
        String key = LOCK_PREFIX + name;
        stringRedisTemplate.delete(key);
    }
}
