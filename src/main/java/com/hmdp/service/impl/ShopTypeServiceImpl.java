package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

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
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getTypeList() {
        String typeListKey = TYPE_LIST_KEY;

        // 1. 从 Redis 查
        String typeListStr = stringRedisTemplate.opsForValue().get(TYPE_LIST_KEY);

        // 有 -> 返回
        if (typeListStr != null) {
            List<ShopType> shopTypeList = JSONUtil.toList(typeListStr, ShopType.class);
            return Result.ok(shopTypeList);
        }

        // 没有 -> 查数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        System.out.println(shopTypeList);

        // 没有 -> 报错
        if (shopTypeList == null)
            return Result.fail("失败");

        // 有 -> 写入 Redis
        typeListStr = JSONUtil.toJsonStr(shopTypeList);
        stringRedisTemplate.opsForValue().set(TYPE_LIST_KEY, typeListStr);

        // 返回
        return Result.ok(shopTypeList);
    }
}
