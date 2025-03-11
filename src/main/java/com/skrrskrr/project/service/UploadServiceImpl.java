package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.PlayListDTO;
import com.skrrskrr.project.dto.UploadDTO;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.repository.MemberTrackRepository;
import com.skrrskrr.project.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class UploadServiceImpl implements UploadService {

    private final TrackService trackService;
    private final PlayListService playListService;
    private final FileService fileService;
    private final MemberService memberService;


    @Value("${upload.path}")
    private String uploadPath;

    @Value("${stream.server.url}")
    private String streamServerUrl;

    @PersistenceContext
    EntityManager em;

    private String generateUUID() {
        return String.valueOf(UUID.randomUUID());
    }

    @Override
    public Map<String, Object> trackUpload(UploadDTO uploadDTO) {

        Map<String, Object> hashMap = new HashMap<>();
        String uuid = generateUUID();

        try {
            // 마지막 트랙 id 얻기
            Long lastTrackId = trackService.getTrackLastId();

            // 트랙 이미지 업로드
            if (uploadDTO.getUploadImage() != null) {
                uploadTrackImage(uploadDTO, lastTrackId, uuid);
            }

            //트랙 업로드
            uploadTrackFile(uploadDTO, lastTrackId, uuid);

            hashMap.put("status", "200");
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
            return hashMap;
        }

    }


    public Map<String, Object> albumUpload(UploadDTO uploadDTO) {

        Map<String, Object> hashMap = new HashMap<>();
        String imageUuid = generateUUID();

        // 마지막 트랙 id 얻기
        Long lastTrackId = trackService.getTrackLastId();

        // 앨범 이미지 업로드
        if (uploadDTO.getUploadImage() != null) {
            uploadTrackImage(uploadDTO, lastTrackId, imageUuid);
        }


        try {
            List<Long> uploadTrackIdList = new ArrayList<>();
            //앨범 트랙 업로드
            for (int i = 0; i < uploadDTO.getUploadFileList().size(); i++) {
                String fileUuid = generateUUID();
                Long uploadTrackId = uploadAlbumTrackFile(uploadDTO, lastTrackId, fileUuid, i);
                uploadTrackIdList.add(uploadTrackId);
            }

            // 앱럼 정보 저장
            saveAlbum(uploadDTO,uploadTrackIdList);

            hashMap.put("status", "200");
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
            return hashMap;
        }


    }

    @Override
    public Map<String, Object> updateTrackImage(UploadDTO uploadDTO) {
        Map<String,Object> hashMap = new HashMap<>();
        String uuid = generateUUID();
        hashMap.put("imagePath",null);

        //서버 이미지 저장
        if(uploadDTO.getUploadImage() != null){
            boolean isTrackImageUpload = fileService.uploadTrackImageFile(uploadDTO.getUploadImage(),"/trackImage",uuid);

            if (isTrackImageUpload){
                uploadDTO.setUploadImagePath(uploadPath + "/trackImage/" + uuid);
                hashMap.putAll(trackService.updateTrackImage(uploadDTO));
                if ("200".equals(hashMap.get("status"))) {
                    String trackImagePath = uploadDTO.getUploadImagePath();
                    hashMap.put("imagePath",trackImagePath);
                }
            }
        }


        return hashMap;
    }

    @Override
    public Map<String, Object> updateMemberImage(UploadDTO uploadDTO) {

        Map<String,Object> hashMap = new HashMap<>();
        String uuid = generateUUID();
        hashMap.put("imagePath",null);

        //서버 이미지 저장
        if(uploadDTO.getUploadImage() != null){
            boolean isMemberImageUpload = fileService.uploadTrackImageFile(uploadDTO.getUploadImage(),"/memberImage",uuid);

            if (isMemberImageUpload){
                uploadDTO.setUploadImagePath(uploadPath + "/memberImage/" + uuid);
                hashMap.putAll(memberService.setMemberImage(uploadDTO));
                if("200".equals(hashMap.get("status"))){
                    String memberImagePath = uploadDTO.getUploadImagePath();
                    hashMap.put("imagePath",memberImagePath);
                }
            }

        }

        return hashMap;
    }


    private void uploadTrackImage(UploadDTO uploadDTO, Long lastTrackId, String uuid)  {
        String imagePath = uploadPath + "/trackImage/" + lastTrackId + "/" + uuid;
        uploadDTO.setUploadImagePath(imagePath);
        fileService.uploadTrackImageFile(uploadDTO.getUploadImage(), "/trackImage/" + lastTrackId, uuid);
    }

    private void uploadTrackFile(UploadDTO uploadDTO, Long lastTrackId, String uuid)  {
        String audioFilePath = streamServerUrl + lastTrackId + "/playList.m3u8";
        uploadDTO.setUploadFilePath(audioFilePath);
        trackService.saveTrack(uploadDTO);
        fileService.uploadTrackFile(uploadDTO.getUploadFile(), "/track/", lastTrackId, uuid);
    }


    private Long uploadAlbumTrackFile(UploadDTO uploadDTO, Long lastTrackId, String uuid, int fileIdx)  {
        String audioFilePath = streamServerUrl + lastTrackId + "/playList.m3u8";
        uploadDTO.setUploadFilePath(audioFilePath);

        String trackNm = getUploadTrackFileNm(uploadDTO.getUploadFileList().get(fileIdx));
        uploadDTO.setTrackNm(trackNm);

        Map<String, Object> trackInfoMap = trackService.saveTrack(uploadDTO);

        if (trackInfoMap.get("status").equals("200")) {
            fileService.uploadTrackFile(uploadDTO.getUploadFileList().get(fileIdx), "/track/", lastTrackId, uuid);
        }

        return (Long) trackInfoMap.get("trackId");

    }

    private void saveAlbum(UploadDTO uploadDTO, List<Long> uploadTrackIdList) {
        PlayListDTO playListDTO = PlayListDTO.builder()
                .playListNm(uploadDTO.getAlbumNm())
                .isPlayListPrivacy(uploadDTO.isTrackPrivacy())
                .memberId(uploadDTO.getMemberId())
                .isAlbum(true)
                .build();

        Map<String, Object> returnMap = playListService.newPlayList(playListDTO);
        if (returnMap.get("status").equals("200")) {
            playListDTO.setPlayListId((Long) returnMap.get("playListId"));

            for (Long trackId : uploadTrackIdList) {
                playListDTO.setTrackId(trackId);
                playListService.setPlayListTrack(playListDTO); //id랑 trackId
            }
        }
    }



    private String getUploadTrackFileNm(MultipartFile trackFile){

        String originalFilename = trackFile.getOriginalFilename();
        assert originalFilename != null;
        int dotIndex = originalFilename.lastIndexOf('.');

        return dotIndex != -1
                ? originalFilename.substring(0, dotIndex)
                : originalFilename;
    }



}
