package com.skrrskrr.project.redisService;

import com.skrrskrr.project.dto.TrackRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisServiceImpl implements RedisService{


    private final RedisTemplate<String,String> redisTemplate;

    @Override
    public Map<String, Object> setLastListenTrackId(TrackRequestDto trackRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            saveLastListenTrackId(trackRequestDto);
            saveLastListenTrackIdList(trackRequestDto);
            hashMap.put("status", "200");
        }  catch (Exception e) {
            // 그 외 모든 예외 처리
            hashMap.put("status", "500");
            log.error("Unexpected error: ", e);
        }

        return hashMap;
    }

    @Override
    public Map<String, Object> setAudioPlayerTrackIdList(TrackRequestDto trackRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();
        try {

            String key = "audioPlayerTrackId:" + trackRequestDto.getLoginMemberId();
            List<Long> trackIdList = trackRequestDto.getTrackIdList();
            redisTemplate.opsForValue().set(key, trackIdList.toString());
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");

        }

        return hashMap;
    }

    @Override
    public List<Long> getAudioPlayerTrackIdList(TrackRequestDto trackRequestDto) {
        // 레디스 키 생성
        String key = "audioPlayerTrackId:" + trackRequestDto.getLoginMemberId();

        // 레디스에서 저장된 데이터를 가져오기
        String trackIdListString = redisTemplate.opsForValue().get(key);

        // String 데이터를 원래 List<Long> 형식으로 복원
        if (trackIdListString != null && !trackIdListString.isEmpty()) {
            return Arrays.stream(trackIdListString
                            .replace("[", "")
                            .replace("]", "")
                            .split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

        // trackIdList가 없으면 빈 리스트 반환
        return new ArrayList<>();
    }


    private void saveLastListenTrackId(TrackRequestDto trackRequestDto) {
        try {
            // Redis에 저장
            String key = "lastListenTrack:" + trackRequestDto.getLoginMemberId();
            redisTemplate.opsForValue().set(key, trackRequestDto.getTrackId().toString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void saveLastListenTrackIdList(TrackRequestDto trackRequestDto) {
        try {

            String key = "lastListenTrackList:" + trackRequestDto.getLoginMemberId();
            String trackId = trackRequestDto.getTrackId().toString();


            redisTemplate.opsForList().remove(key, 1, trackId);

            Long listSize = redisTemplate.opsForList().size(key);

            if (listSize == null) {
                listSize = 0L;
            }

            if (listSize >= 30) {
                redisTemplate.opsForList().rightPop(key);
            }

            redisTemplate.opsForList().leftPush(key, trackId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<Long> getLastListenTrackIdList(TrackRequestDto trackRequestDto){

        String key = "lastListenTrackList:" + trackRequestDto.getLoginMemberId();
        List<String> lastListenTrackList = redisTemplate.opsForList().range(key, 0, -1); // 모든 트랙 ID를 가져옵니다.

        return  lastListenTrackList.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }


    @Override
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
