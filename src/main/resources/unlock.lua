local key = KEYS[1]
local uuid = ARGV[1]
local redisUuid = redis.call('get',key)
if(uuid == redisUuid) then
    return redis.call('del',key)
end
return 0