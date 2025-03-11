package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.TrackDTO;
import com.skrrskrr.project.dto.TrackSearchDTO;
import com.skrrskrr.project.dto.UploadDTO;
import com.skrrskrr.project.entity.TrackLike;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;


public interface TrackService {


    Map<String,Object> saveTrack(UploadDTO uploadDTO);

    Map<String,Object> updateTrackImage(UploadDTO uploadDTO);

    Map<String,Object> setTrackinfo(TrackDTO trackDTO);

    Long getTrackLastId();

    Map<String,String> setTrackLike(Long memberId, Long trackId);

    Map<String,Object> getLikeTrack(Long memberId,Long listIndex);

    List<TrackDTO> getLikeTrackList(Long memberId,Long listIndex);

    Long getLikeTrackListCnt(Long memberId);

    Map<String,Object> setLockTrack(TrackDTO trackDTO);

    Map<String,Object> getTrackInfo(Long trackId, Long memberId);

    Map<String,Object> getUploadTrack(Long memberId, Long listIndex);

    List<TrackDTO> getUploadTrackList(Long memberId, Long listIndex);

    Long getUploadTrackListCnt(Long memberId);

    TrackLike getTrackLikeStatus(Long memberId, Long trackId);

    List<TrackDTO> getRecommendTrackList(Long memberId, Long trackId, Long trackCategoryId);


}
