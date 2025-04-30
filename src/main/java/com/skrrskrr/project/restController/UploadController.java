package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.MemberRequestDto;
import com.skrrskrr.project.dto.MemberResponseDto;
import com.skrrskrr.project.dto.TrackResponseDto;
import com.skrrskrr.project.dto.UploadDto;
import com.skrrskrr.project.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Log4j2
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/api/trackUpload")
    public ResponseEntity<Void> trackUpload(@ModelAttribute UploadDto uploadDto) throws Exception {
        log.info("trackUpload");
        uploadService.trackUpload(uploadDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/albumUpload")
    public ResponseEntity<Void> albumUpload(@ModelAttribute UploadDto uploadDto) throws Exception {
        log.info("albumUpload");
        uploadService.albumUpload(uploadDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/updateTrackImage")
    public ResponseEntity<TrackResponseDto> updateTrackImage(@ModelAttribute UploadDto uploadDto){
        log.info("setTrackImage");
        TrackResponseDto trackResponseDto = uploadService.updateTrackImage(uploadDto);
        return ResponseEntity.ok(trackResponseDto);
    }

    @PostMapping("/api/updateMemberImage")
    public ResponseEntity<MemberResponseDto> updateMemberImage(@ModelAttribute UploadDto uploadDto){
        log.info("updateMemberImage");
        MemberResponseDto memberResponseDto = uploadService.updateMemberImage(uploadDto);
        return ResponseEntity.ok(memberResponseDto);

    }
}
