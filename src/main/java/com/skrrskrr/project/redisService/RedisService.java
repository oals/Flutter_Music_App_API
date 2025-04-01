package com.skrrskrr.project.redisService;

import com.skrrskrr.project.dto.TrackRequestDto;

import java.util.List;
import java.util.Map;

public interface RedisService {

    Map<String,Object> setLastListenTrackId(TrackRequestDto trackRequestDto);

    Map<String,Object> getLastListenTrackId(TrackRequestDto trackRequestDto);

    List<Long> getLastListenTrackIdList(TrackRequestDto trackRequestDto);


}
