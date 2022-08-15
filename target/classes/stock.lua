-- 判断库存的lua脚本
if (redis.call("exists", KEYS[1]) == 1) then --如果该商品存在
    local stock = tonumber(redis.call("get", KEYS[1]));
    if (stock > 0) then --若库存大于0
        redis.call("incrby", KEYS[1], -1); --减库存
        return stock;
    end;
    return 0;
end;