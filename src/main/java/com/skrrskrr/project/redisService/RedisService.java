package com.skrrskrr.project.redisService;

import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.dto.TrackResponseDto;

import java.util.List;
import java.util.Map;

public interface RedisService {

    void setLastListenTrackId(TrackRequestDto trackRequestDto);

    void setAudioPlayerTrackIdList(TrackRequestDto trackRequestDto);

    List<Long> getAudioPlayerTrackIdList(TrackRequestDto trackRequestDto);

    TrackResponseDto getLastListenTrackId(TrackRequestDto trackRequestDto);

    List<Long> getLastListenTrackIdList(TrackRequestDto trackRequestDto);


}
