package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdGenerator;
import com.hmdp.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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

    @Override
    public Result addSeckillOrder(long id) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);

        LocalDateTime now = LocalDateTime.now();

        if(now.isBefore(seckillVoucher.getBeginTime()))
            return Result.fail("没开始卖呢");
        if(now.isAfter(seckillVoucher.getEndTime()))
            return Result.fail("活动结束了");
        if(seckillVoucher.getStock() < 1)
            return Result.fail("卖完了");

        seckillVoucherService.update().set("stock", seckillVoucher.getStock() - 1).eq("voucher_id", id);

        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(redisIdGenerator.nextId("hmdp:voucher:order"));
        voucherOrder.setUserId(UserHolder.getUser().getId());
        voucherOrder.setVoucherId(id);
        save(voucherOrder);

        long voucherOrderId = voucherOrder.getId();
        return Result.ok(voucherOrderId);
    }
}
