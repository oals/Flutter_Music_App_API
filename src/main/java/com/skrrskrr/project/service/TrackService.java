package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.TrackDTO;
import com.skrrskrr.project.dto.UploadDTO;

import java.util.HashMap;


public interface TrackService {


    HashMap<String,Object> trackUpload(UploadDTO uploadDTO);

    HashMap<String,Object> setTrackImage(UploadDTO uploadDTO);

    HashMap<String,Object> setTrackinfo(TrackDTO trackDTO);

    HashMap<String,String> setTrackLike(Long memberId, Long trackId);

    HashMap<String,Object> getLikeTrack(Long memberId,Long listIndex);

    HashMap<String,Object> getTrackInfo(Long trackId, Long memberId);

    HashMap<String,Object>  getUploadTrack(Long memberId, Long listIndex);




}
