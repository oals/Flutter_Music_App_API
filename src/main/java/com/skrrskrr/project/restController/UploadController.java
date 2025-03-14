package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.UploadDto;
import com.skrrskrr.project.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    public Map<String, Object> trackUpload(@ModelAttribute UploadDto uploadDto) throws Exception {
        log.info("trackUpload");
        return uploadService.trackUpload(uploadDto);
    }


    @PostMapping("/api/albumUpload")
    public Map<String, Object> albumUpload(@ModelAttribute UploadDto uploadDto) throws Exception {
        log.info("albumUpload");
        return uploadService.albumUpload(uploadDto);
    }



    @PostMapping("/api/updateTrackImage")
    public Map<String,Object> updateTrackImage(@ModelAttribute UploadDto uploadDto){
        log.info("setTrackImage");
        return uploadService.updateTrackImage(uploadDto);
    }


    @PostMapping("/api/setMemberImage")
    public Map<String,Object> updateMemberImage(@ModelAttribute UploadDto uploadDto){
        log.info("setMemberImage");
        return uploadService.updateMemberImage(uploadDto);

    }



}
