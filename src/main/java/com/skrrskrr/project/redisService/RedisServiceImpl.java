package com.skrrskrr.project.redisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap; import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisServiceImpl implements RedisService{


    private final RedisTemplate<String,String> redisTemplate;


    public Map<String,Object> setRecentTrack(){
//        redisTemplate.opsForValue();
        return null;
    }

    public Map<String,Object> getRecentTrack(){
//        redisTemplate.op
        return null;
    }




}
