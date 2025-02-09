package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.UploadDTO;
import com.skrrskrr.project.service.MemberService;
import com.skrrskrr.project.service.TrackService;
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
    final private MemberService memberService;


    @PostMapping("/api/setTrackImage")
    public Map<String,Object> setTrackImage(@ModelAttribute UploadDTO uploadDTO){

        Map<String,Object> hashMap = new HashMap<>();
        hashMap.put("imagePath",null);
        String uuid =  String.valueOf(UUID.randomUUID());

        //서버 이미지 저장
        if(uploadDTO.getUploadImage() != null){
            boolean isTrackImageUpload = uploadFile(uploadDTO.getUploadImage(),"/trackImage",uuid);
            if (isTrackImageUpload){
                uploadDTO.setUploadImagePath("C:/uploads/trackImage/" + uuid);
                hashMap = trackService.setTrackImage(uploadDTO);
                if ("200".equals(hashMap.get("status"))) {
                    String trackImagePath = uploadDTO.getUploadImagePath();
                    hashMap.put("imagePath",trackImagePath);
                }
            }
        }

        return hashMap;
    }


    @PostMapping("/api/setMemberImage")
    public Map<String,Object> setMemberImage(@ModelAttribute UploadDTO uploadDTO){

        Map<String,Object> hashMap = new HashMap<>();
        String uuid =  String.valueOf(UUID.randomUUID());
        hashMap.put("imagePath",null);

        //서버 이미지 저장
        if(uploadDTO.getUploadImage() != null){
            boolean isMemberImageUpload = uploadFile(uploadDTO.getUploadImage(),"/memberImage",uuid);

            if (isMemberImageUpload){
                uploadDTO.setUploadImagePath("C:/uploads/memberImage/" + uuid);
                hashMap = memberService.setMemberImage(uploadDTO);
                if(hashMap.get("status").equals("200")){
                    String memberImagePath = uploadDTO.getUploadImagePath();
                    hashMap.put("imagePath",memberImagePath);
                }
            }

        }

        return hashMap;

    }



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


    //음원, 이미지 업로드
    public boolean uploadFile(MultipartFile file, String dir, String trackNm) {
        if (file.isEmpty()) {
            throw new RuntimeException("파일이 비어 있습니다.");
        }

        // 저장할 경로 설정
        String uploadDir = "C:/uploads" + dir; // 원하는 경로로 변경
        File uploadDirectory = new File(uploadDir);

        // 디렉토리가 없으면 생성
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // 파일 이름 생성 (예: 원래 파일 이름)
        String fileType = "";

        if (Objects.equals(file.getContentType(), "audio/mpeg")) {
            fileType = ".mp3";
        } else if (Objects.equals(file.getContentType(), "video/mp4")) {
            fileType = ".mp4";
        } else if (Objects.equals(file.getContentType(), "image/jpeg")) {
            fileType = ".jpg";
        } else if (Objects.equals(file.getContentType(), "image/png")) {
            fileType = ".png";
        } else {
            fileType = ".mp3"; // 기본값
        }


        File destFile = new File(uploadDir, trackNm + fileType);

        try {
            // 파일 저장
            file.transferTo(destFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
