package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.UploadDto;

import java.util.Map;

public interface UploadService {

    Map<String,Object> trackUpload(UploadDto uploadDto);

    Map<String,Object> albumUpload(UploadDto uploadDto);

    Map<String,Object> updateTrackImage(UploadDto uploadDto);

    Map<String,Object> updateMemberImage(UploadDto uploadDto);


}
