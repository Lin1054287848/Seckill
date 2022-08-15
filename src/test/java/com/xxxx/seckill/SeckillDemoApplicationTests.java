package com.xxxx.seckill;

import com.xxxx.seckill.utils.UUIDUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;


import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SeckillDemoApplicationTests {
    /**
     *redis的分布式锁SETNX
     * 1. SETNX
     * 2.为了解决线程获得SETNX 后 异常出现死锁 -> 设置过期时间（设置锁和设置过期时间也需要保证原子性 采用：SEXNT EX 或 lua脚本）
     * 3.为了解决线程获得SETNX 后 释放其他线程获得的锁 -> key中的值设置为唯一的value值（如：UUID或者线程ID） 释放前判断 value值是否为自己线程持有的
     * 4.释放锁的两个步骤：比较锁，删除锁，因此释放锁不是原子性的，可能导致出现并发问题 ，采用lua脚本
     * 5.设置了过期时间，但是线程任务没有执行完，key到期了 ->采用守护线程（看门狗）为redis的key过期时间续期
     */

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisScript<Boolean> redisScript;

    @Test
    public void contextLoads() {
        //SETNX (redis的分布式锁) (SET if not exist)
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String s = UUID.randomUUID().toString();
        //占位，如果key不存在才能设置成功
        Boolean isLock = valueOperations.setIfAbsent("k1", s, 120, TimeUnit.SECONDS); //设置过期时间防止死锁
        //如果占位成功，进位操作
        if (isLock) {
            valueOperations.set("name", "xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println("name = " + name);
            System.out.println(valueOperations.get("k1"));
            Boolean result = (Boolean) redisTemplate.execute(redisScript, Collections.singletonList("k1"), s); //执行完线程任务，执行lua脚本（对比锁，删除锁），删除锁
            System.out.println(result);
        }else {
            System.out.println("有线程在使用，请稍后");
        }



    }


    public int[] singleNumber(int[] nums) {
        int result = 0;
        for (int i = 0; i < nums.length; i++) {
            result = result ^ nums[i];
        }
        int rightOne = result & (~result + 1);//提取result最右的1
        //System.out.println(rightOne);
        int firstResult = 0;
        for (int num : nums) {
            if ((rightOne & num) == rightOne) {
                firstResult ^= num;
            }
        }
        return new int[]{firstResult, result ^ firstResult};
    }

}
