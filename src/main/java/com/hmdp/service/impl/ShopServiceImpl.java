package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;



    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @Override
    /**
     * 实现了缓存击穿(逻辑过期)
     */
    public Result getShopById(Long id){
        String shopKey = CACHE_SHOP_KEY + id;
        // 1. 从 Redis 查
        String shopStr = stringRedisTemplate.opsForValue().get(shopKey);

        if (StrUtil.isBlank(shopStr)) {
            return null;
        }
        RedisData redisData = JSONUtil.toBean(shopStr, RedisData.class);

        JSONObject data = (JSONObject) redisData.getData();
        Shop shop = JSONUtil.toBean(data, Shop.class);

        LocalDateTime expireTime = redisData.getExpireTime();

        // 未过期
        if(expireTime.isAfter(LocalDateTime.now())) {
            return Result.ok(shop);
        }

        String lockKey = LOCK_SHOP_KEY + id;
        boolean lock = tryLock(lockKey);

        if (lock) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try{
                    saveShop2Redis(id,60L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally{
                    unlock(lockKey);
                }
            });
        }
        System.out.println(shop.getName());
        return Result.ok(shop);


    }


//    @Override
//    /**
//     * 实现了缓存击穿(互斥锁)
//     */
//    public Result getShopById(Long id){
//        String shopKey = SHOP_ID_KEY + id;
//        // 1. 从 Redis 查
//        String shopStr = stringRedisTemplate.opsForValue().get(shopKey);
//
//        // redis 里有这个记录 (null和""都可以通过这次检查)
//        if (StrUtil.isNotBlank(shopStr)) {
//            Shop shop = JSONUtil.toBean(shopStr, Shop.class);
//            return Result.ok(shop);
//        }
//
//        // 再挑选出""，说明是为了避免缓存穿透设计的空字符串
//        if (shopStr != null) {
//            return Result.fail("没有这个店铺");
//        }
//
//
//        String lockKey = "lock:shop:" + id;
//        Shop shop = null;
//        try{
//            boolean lock = tryLock(lockKey);
//            if (!lock) {
//                Thread.sleep(50);
//                getShopById(id);
//            }
//
//            // 没有 -> 查数据库
//            shop = getById(id);
//
//            // 没有 -> 将空值写入 redis
//            // 模拟重建延迟
//            Thread.sleep(200);
//            if (shop == null){
//                stringRedisTemplate.opsForValue().set(shopKey, "",DEFAULT_TTL,DEFAULT_UNIT);
//                return Result.fail("没这个店");
//            }
//
//            // 有 -> 写入 Redis
//            stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop),DEFAULT_TTL,DEFAULT_UNIT);
//
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally{
//            unlock(lockKey);
//        }
//
//        // 返回
//        return Result.ok(shop);
//    }


//    @Override
//    /**
//     * 实现了缓存穿透
//     */
//    public Result getShopById(Long id) {
//        String shopKey = SHOP_ID_KEY + id;
//        // 1. 从 Redis 查
//        String shopStr = stringRedisTemplate.opsForValue().get(shopKey);
//
//        // redis 里有这个记录 (null和""都可以通过这次检查)
//        if (StrUtil.isNotBlank(shopStr)) {
//            Shop shop = JSONUtil.toBean(shopStr, Shop.class);
//            return Result.ok(shop);
//        }
//
//        // 再挑选出""，说明是为了避免缓存穿透设计的空字符串
//        if (shopStr != null) {
//            return Result.fail("没有这个店铺");
//        }
//
//
//        // 没有 -> 查数据库
//        Shop shop = getById(id);
//
//        // 没有 -> 将空值写入 redis
//        if (shop == null){
//            stringRedisTemplate.opsForValue().set(shopKey, "",DEFAULT_TTL,DEFAULT_UNIT);
//            return Result.fail("没这个店");
//        }
//
//
//        // 有 -> 写入 Redis
//        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop),DEFAULT_TTL,DEFAULT_UNIT);
//
//        // 返回
//        return Result.ok(shop);
//    }



    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        Long id = shop.getId();

        if (id == null)
            return Result.fail("店铺 id 为空");

        // 1. 更新数据库
        updateById(shop);

        // 2. 删除缓存
        stringRedisTemplate.delete(SHOP_ID_KEY + id);
        return Result.ok();
    }

    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", DEFAULT_TTL,DEFAULT_UNIT);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }

    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
        Shop shop = getById(id);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        Thread.sleep(200);
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id,JSONUtil.toJsonStr(redisData));
    }

}
