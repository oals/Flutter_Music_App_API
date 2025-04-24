package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.MemberResponseDto;
import com.skrrskrr.project.dto.TrackResponseDto;
import com.skrrskrr.project.dto.UploadDto;

import java.util.Map;

public interface UploadService {

    void trackUpload(UploadDto uploadDto);

    void albumUpload(UploadDto uploadDto);

    TrackResponseDto updateTrackImage(UploadDto uploadDto);

    MemberResponseDto updateMemberImage(UploadDto uploadDto);


}
