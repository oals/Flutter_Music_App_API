package com.skrrskrr.project.redisService;

import com.skrrskrr.project.dto.TrackRequestDto;
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


    public Map<String,Object> setLastListenTrackId(TrackRequestDto trackRequestDto){

        Map<String, Object> hashMap = new HashMap<>();

        try {
            // Redis에 저장
            String key = "lastListenTrack:" + trackRequestDto.getLoginMemberId();
            // 트랙 ID만 Redis에 저장 (사용자별로)
            redisTemplate.opsForValue().set(key, trackRequestDto.getTrackId().toString());

            hashMap.put("status", "200");
        } catch(Exception e) {
            hashMap.put("status", "500");
            e.printStackTrace();
        }

        return hashMap;
    }

    public Map<String,Object> getLastListenTrackId(TrackRequestDto trackRequestDto){

        Map<String,Object> hashMap = new HashMap<>();

        String key = "lastListenTrack:" + trackRequestDto.getLoginMemberId();

        try{
            String lastListenTrackId = redisTemplate.opsForValue().get(key);
            hashMap.put("lastListenTrackId",lastListenTrackId);
            hashMap.put("status","200");
        } catch(Exception e) {
            hashMap.put("status","500");
        }


        return hashMap;
    }




}
