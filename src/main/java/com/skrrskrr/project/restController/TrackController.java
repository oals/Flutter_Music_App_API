package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.PlayListDTO;
import com.skrrskrr.project.dto.TrackDTO;
import com.skrrskrr.project.dto.TrackSearchDTO;
import com.skrrskrr.project.dto.UploadDTO;
import com.skrrskrr.project.entity.Track;
import com.skrrskrr.project.service.PlayListService;
import com.skrrskrr.project.service.TrackService;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.metadata.Metadata;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.audio.AudioParser;
import org.apache.tika.sax.BodyContentHandler;
import org.codelibs.jhighlight.fastutil.Hash;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Log4j2
public class TrackController {

    private final TrackService trackService;
    private final PlayListService playListService;


    @PostMapping("/api/setTrackInfo")
    public Map<String,Object> setTrackInfo(@RequestBody TrackDTO trackDTO){
        log.info("setTrackInfo");
        return trackService.setTrackinfo(trackDTO);
    }


    @PostMapping("/api/trackUpload")
    public Map<String, Object> trackUpload(@ModelAttribute UploadDTO uploadDTO) throws Exception {
        log.info("trackUpload");
        Map<String,Object> hashMap = new HashMap<>();
        String imageUuid =  String.valueOf(UUID.randomUUID());
        try {
            List<Long> uploadTrackIdList = new ArrayList<>();
            //서버에 음원 저장
            for (int i = 0 ; i < uploadDTO.getUploadFileList().size(); i ++) {
                String uuid =  String.valueOf(UUID.randomUUID());

                if (uploadDTO.getUploadImage() != null){
                    uploadDTO.setUploadImagePath("C:/uploads/trackImage/" + imageUuid);
                }
                uploadDTO.setUploadFilePath("C:/uploads/track/" + uuid);
                if (uploadDTO.isAlbum()) {
                    String originalFilename = uploadDTO.getUploadFileList().get(i).getOriginalFilename();
                    assert originalFilename != null;
                    int dotIndex = originalFilename.lastIndexOf('.');

                    String fileNameWithoutExtension = dotIndex != -1
                            ? originalFilename.substring(0, dotIndex)
                            : originalFilename;

                    uploadDTO.setTrackNm(fileNameWithoutExtension);
                }

                Map<String,Object> returnMap = trackService.trackUpload(uploadDTO);
                if(returnMap.get("status").equals("200")) {
                    uploadTrackIdList.add((Long)returnMap.get("trackId"));
                    uploadFile(uploadDTO.getUploadFileList().get(i),"/track",uuid + i);
                }

            }
            uploadFile(uploadDTO.getUploadImage(),"/trackImage",imageUuid);

            if(uploadDTO.isAlbum()) {

                PlayListDTO playListDTO = PlayListDTO.builder()
                        .playListNm(uploadDTO.getAlbumNm())
                        .isPlayListPrivacy(uploadDTO.isTrackPrivacy())
                        .memberId(uploadDTO.getMemberId())
                        .isAlbum(true)
                        .build();

                Map<String,Object> returnMap = playListService.newPlayList(playListDTO);
                if (returnMap.get("status").equals("200")) {
                    playListDTO.setPlayListId((Long)returnMap.get("playListId"));

                    for (Long trackId : uploadTrackIdList) {
                        playListDTO.setTrackId(trackId);
                        playListService.setPlayListTrack(playListDTO); //id랑 trackId
                    }
                }
            }

            hashMap.put("status","200");
            return hashMap;
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
            return hashMap;
        }
    }


    @GetMapping("/api/getLikeTrack")
    public Map<String,Object> getLikeTrack(@RequestParam Long memberId,@RequestParam Long listIndex){
        log.info("getLikeTrack");
        return trackService.getLikeTrack(memberId,listIndex);
    }

    @GetMapping("/api/setTrackLike")
    public Map<String,String> setTrackLike(@RequestParam Long memberId, @RequestParam Long trackId){
        log.info("setTrackLike");
        return trackService.setTrackLike(memberId,trackId);
    }


    @GetMapping("/api/getTrackInfo")
    public Map<String,Object> getTrackInfo(@RequestParam Long trackId, @RequestParam Long memberId){
        log.info("getTrackInfo");
        return trackService.getTrackInfo(trackId,memberId);
    }

    @GetMapping("/api/getUploadTrack")
    public Map<String,Object>  getUploadTrack(@RequestParam Long memberId, @RequestParam Long listIndex){
        log.info("getUploadTrack");
        return trackService.getUploadTrack(memberId,listIndex);
    }




    //음원, 이미지 업로드
    private boolean uploadFile(MultipartFile file,String dir,String trackNm) {
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
            e.printStackTrace();
            return false;
        }
    }




}
