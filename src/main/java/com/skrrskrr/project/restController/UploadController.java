package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.PlayListDTO;
import com.skrrskrr.project.dto.UploadDTO;
import com.skrrskrr.project.service.FileService;
import com.skrrskrr.project.service.PlayListService;
import com.skrrskrr.project.service.TrackService;
import com.skrrskrr.project.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
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
    public Map<String, Object> trackUpload(@ModelAttribute UploadDTO uploadDTO) throws Exception {
        log.info("trackUpload");
        return uploadService.trackUpload(uploadDTO);
    }


    @PostMapping("/api/albumUpload")
    public Map<String, Object> albumUpload(@ModelAttribute UploadDTO uploadDTO) throws Exception {
        log.info("albumUpload");
        return uploadService.albumUpload(uploadDTO);
    }



    @PostMapping("/api/updateTrackImage")
    public Map<String,Object> updateTrackImage(@ModelAttribute UploadDTO uploadDTO){
        log.info("setTrackImage");
        return uploadService.updateTrackImage(uploadDTO);
    }


    @PostMapping("/api/setMemberImage")
    public Map<String,Object> updateMemberImage(@ModelAttribute UploadDTO uploadDTO){
        log.info("setMemberImage");
        return uploadService.updateMemberImage(uploadDTO);

    }



}
