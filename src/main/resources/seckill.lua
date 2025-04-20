local voucherKey = KEYS[1]
local userSetKey = KEYS[2]
local userId = ARGV[1]

-- 没库存了
if(tonumber(redis.call('get',voucherKey)) < 1) then
    return 1
end

-- 用户买过了
if(redis.call("sismember",userSetKey,userId) == 1) then
    return 2
end

-- 成功下订单
redis.call('incrby',voucherKey,-1)
redis.call('sadd',userSetKey,userId)
return 0