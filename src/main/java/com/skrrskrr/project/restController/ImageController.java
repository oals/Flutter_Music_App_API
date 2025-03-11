package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.UploadDTO;
import com.skrrskrr.project.service.FileService;
import com.skrrskrr.project.service.MemberService;
import com.skrrskrr.project.service.TrackService;
import com.skrrskrr.project.service.UploadService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap; import java.util.Map;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Log4j2
public class ImageController {

    final private TrackService trackService;
    final private UploadService uploadService;
    final private MemberService memberService;
    final private FileService fileService;


    //이미지 로더
    @GetMapping("/viewer/imageLoader")
    public ResponseEntity<FileSystemResource> getImage(@RequestParam String trackImagePath) {

        log.info("imageLoader 호출");

        File imageFile = new File(trackImagePath + ".jpg");

        if (!imageFile.exists()) {
            log.info("image_not_found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        FileSystemResource resource = new FileSystemResource(imageFile);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "image/jpg"); // 또는 실제 이미지 타입에 맞게 설정

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }


}
