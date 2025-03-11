package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.UploadDTO;

import java.util.Map;

public interface UploadService {

    Map<String,Object> trackUpload(UploadDTO uploadDTO);

    Map<String,Object> albumUpload(UploadDTO uploadDTO);

    Map<String,Object> updateTrackImage(UploadDTO uploadDTO);

    Map<String,Object> updateMemberImage(UploadDTO uploadDTO);


}
