package com.hmdp.utils;


import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Lockv1 implements ILock {

    String name;
    String LOCK_PREFIX = "lock:";
    StringRedisTemplate stringRedisTemplate;
    String uuid = UUID.randomUUID().toString(true);
    private static final DefaultRedisScript<Long> luaScript;

    static{
        luaScript = new DefaultRedisScript<>();
        luaScript.setLocation(new ClassPathResource("unlock.lua"));
        luaScript.setResultType(Long.class);
    }

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
        String key = LOCK_PREFIX + name;
        String myValue = uuid + Thread.currentThread().getId();
        stringRedisTemplate.execute(
                luaScript,
                Collections.singletonList(key),
                myValue
                );
    }
}
