package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdGenerator;
import com.hmdp.utils.UserHolder;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addSeckillOrder(long id) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);

        LocalDateTime now = LocalDateTime.now();

        if(now.isBefore(seckillVoucher.getBeginTime()))
            return fail("没开始卖呢");
        if(now.isAfter(seckillVoucher.getEndTime()))
            return fail("活动结束了");
        if(seckillVoucher.getStock() < 1)
            return fail("卖完了");

        boolean success = seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id", id).update();

        if(success){
            VoucherOrder voucherOrder = new VoucherOrder();
            voucherOrder.setId(redisIdGenerator.nextId("hmdp:voucher:order"));
            voucherOrder.setUserId(UserHolder.getUser().getId());
            voucherOrder.setVoucherId(id);
            save(voucherOrder);
            long voucherOrderId = voucherOrder.getId();
            return Result.ok(voucherOrderId);
        }


        return Result.fail("失败");

    }
}
