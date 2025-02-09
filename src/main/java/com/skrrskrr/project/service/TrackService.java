package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.TrackDTO;
import com.skrrskrr.project.dto.UploadDTO;

import java.util.HashMap; import java.util.Map;
import java.util.Map;


public interface TrackService {


    Map<String,Object> trackUpload(UploadDTO uploadDTO);

    Map<String,Object> setTrackImage(UploadDTO uploadDTO);

    Map<String,Object> setTrackinfo(TrackDTO trackDTO);

    Map<String,String> setTrackLike(Long memberId, Long trackId);

    Map<String,Object> getLikeTrack(Long memberId,Long listIndex);

    Map<String,Object> getTrackInfo(Long trackId, Long memberId);

    Map<String,Object>  getUploadTrack(Long memberId, Long listIndex);




}
