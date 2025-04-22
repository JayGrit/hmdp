local voucherKey = KEYS[1]
local userSetKey = KEYS[2]
local userId = ARGV[1]
local orderId = ARGV[2]
local voucherId = ARGV[3]

-- 没库存了
if(tonumber(redis.call('get',voucherKey)) < 1) then
    return 1
end

-- 用户买过了
if(redis.call("sismember",userSetKey,userId) == 1) then
    return 2
end

-- 成功下订单
redis.call('INCRBY',voucherKey,-1)
redis.call('SADD',userSetKey,userId)

redis.call('XADD','hmdp:stream:orders','*','userId',userId,'voucherId',voucherId,'id',orderId)

return 0