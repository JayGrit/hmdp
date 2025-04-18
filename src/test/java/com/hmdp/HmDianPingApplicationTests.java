package com.hmdp;

import com.hmdp.service.IShopService;
import com.hmdp.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Test
    void contextLoads() throws InterruptedException {
        shopService.saveShop2Redis(1L,100L);
    }

}
