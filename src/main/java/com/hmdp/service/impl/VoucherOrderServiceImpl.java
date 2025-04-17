package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdGenerator;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static com.hmdp.dto.Result.fail;

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

    @Override
    public Result addSeckillOrder(long id) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);

        LocalDateTime now = LocalDateTime.now();

        if(now.isBefore(seckillVoucher.getBeginTime()))
            return fail("没开始卖呢");
        if(now.isAfter(seckillVoucher.getEndTime()))
            return fail("活动结束了");
        if(seckillVoucher.getStock() < 1)
            return fail("卖完了");

        Long userId = UserHolder.getUser().getId();
        synchronized (userId.toString().intern()) {
            return proxy.createVoucherOrder(id);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public Result createVoucherOrder(long id) {
        long userId = UserHolder.getUser().getId();
        int orderByUserCount = query()
                .eq("voucher_id", id)
                .eq("user_id", userId)
                .count();

        if(orderByUserCount > 0)
            return Result.fail("你已经买过了");


        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", id)
                .gt("stock", 0)
                .update();

        if(!success)
            return Result.fail("库存不足");


        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(redisIdGenerator.nextId("hmdp:voucher:order"));
        voucherOrder.setUserId(UserHolder.getUser().getId());
        voucherOrder.setVoucherId(id);
        save(voucherOrder);
        long voucherOrderId = voucherOrder.getId();


        return Result.ok(voucherOrderId);
    }
}
