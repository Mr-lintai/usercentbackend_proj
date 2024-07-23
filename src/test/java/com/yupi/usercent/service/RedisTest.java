package com.yupi.usercent.service;

import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * @author lintai
 * @version 1.0
 */
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisson(){
        //redisTemplate.opsForValue();
    }
}
