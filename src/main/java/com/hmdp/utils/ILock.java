package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

public interface ILock {

    boolean tryLock();

    void delLock();
}
