package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdGenerator;
import com.hmdp.utils.UserHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static com.hmdp.utils.RedisConstants.SECKILL_ORDER_KEY;
import static com.hmdp.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private SeckillVoucherServiceImpl seckillVoucherService;

    @Resource
    private RedisIdGenerator redisIdGenerator;

    @Resource
    private IVoucherOrderService proxy;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    private static DefaultRedisScript<Long> seckillScript = new DefaultRedisScript<>();
    static {
        seckillScript.setLocation(new ClassPathResource("seckill.lua"));
        seckillScript.setResultType(Long.class);
    }

    private BlockingQueue<VoucherOrder> ordersQueue = new ArrayBlockingQueue<>(1024*1024);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();


    @PostConstruct
    public void init() {
        executorService.submit(new OrderTask());
    }

    private class OrderTask implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    VoucherOrder order = ordersQueue.take();
                    proxy.createVoucherOrder(order);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public Result addSeckillOrder(long id) {
        UserDTO user = UserHolder.getUser();

        List<String> keys = new ArrayList<>();
        keys.add(SECKILL_STOCK_KEY + id);
        keys.add(SECKILL_ORDER_KEY + id);

        long result = stringRedisTemplate.execute(
                seckillScript,
                keys,
                user.getId().toString()
        );
        switch((int) result){
            case 1:
                return Result.fail("库存不足");
            case 2:
                return Result.fail("您已经购买");
            case 0:{
                VoucherOrder order = new VoucherOrder();
                long orderId = redisIdGenerator.nextId("hmdp:voucher:order");
                order.setId(orderId);
                order.setVoucherId(id);
                order.setUserId(user.getId());
                ordersQueue.add(order);
                return Result.ok(orderId);
            }

        }
        return Result.fail("其他错误");
    }





    @Transactional(rollbackFor = Exception.class)
    public void createVoucherOrder(VoucherOrder order) {

        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", order.getVoucherId())
                .gt("stock", 0)
                .update();

        if(!success)
            System.out.println("数据库写入报错");

        save(order);
    }
}
