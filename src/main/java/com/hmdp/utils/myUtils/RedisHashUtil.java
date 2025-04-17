package com.hmdp.utils.myUtils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.hmdp.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

public final class RedisHashUtil {

    private RedisHashUtil() {};

    public static void saveBeanToHashMap(StringRedisTemplate template, String key, Object bean, Long ttl, TimeUnit timeUnit){
        Map<String,Object> userMap = BeanUtil.beanToMap(bean,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString())
        );

        template.opsForHash().putAll(key,userMap);
        template.expire(key, ttl, timeUnit);
    }

    public static void saveBeanToHashMap(StringRedisTemplate template, String key, Object bean){
        saveBeanToHashMap(template,key,bean,DEFAULT_TTL,DEFAULT_UNIT);
    }



}
