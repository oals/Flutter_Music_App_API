package com.skrrskrr.project.service;


import com.skrrskrr.project.dto.PlayListDto;
import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.dto.UploadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class UploadServiceImpl implements UploadService {

    private final TrackService trackService;
    private final PlayListService playListService;
    private final FileService fileService;
    private final MemberService memberService;

    @Value("${UPLOAD_PATH}")
    private String uploadPath;

    @Value("${STREAM_SERVER_URL}")
    private String streamServerUrl;

    private String generateUUID() {
        return String.valueOf(UUID.randomUUID());
    }

    @Override
    public Map<String, Object> trackUpload(UploadDto uploadDto) {

        Map<String, Object> hashMap = new HashMap<>();
        String uuid = generateUUID();

        try {
            // 트랙 이미지 업로드
            Long lastTrackId = uploadImage(uploadDto, uuid);

            //트랙 업로드
            uploadTrackFile(uploadDto, lastTrackId, uuid);

            hashMap.put("status", "200");
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
            return hashMap;
        }

    }

    @Override
    public Map<String, Object> albumUpload(UploadDto uploadDto) {

        Map<String, Object> hashMap = new HashMap<>();
        String uuid = generateUUID();

        // 앨범 이미지 업로드
        Long lastTrackId = uploadImage(uploadDto, uuid);

        try {
            List<Long> uploadTrackIdList = new ArrayList<>();
            //앨범 트랙 업로드
            for (int i = 0; i < uploadDto.getUploadFileList().size(); i++) {
                String fileUuid = generateUUID();
                Long uploadTrackId = uploadAlbumTrackFile(uploadDto, lastTrackId, fileUuid, i);
                uploadTrackIdList.add(uploadTrackId);
            }

            // 앱럼 정보 저장
            saveAlbum(uploadDto,uploadTrackIdList);

            hashMap.put("status", "200");
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
            return hashMap;
        }


    }

    @Override
    public Map<String, Object> updateTrackImage(UploadDto uploadDto) {
        Map<String,Object> hashMap = new HashMap<>();
        String uuid = generateUUID();
        hashMap.put("imagePath",null);

        //서버 이미지 저장
        if(uploadDto.getUploadImage() != null){
            Boolean isTrackImageUpload = fileService.uploadTrackImageFile(uploadDto.getUploadImage(),"/trackImage",uuid);

            if (isTrackImageUpload){
                uploadDto.setUploadImagePath(uploadPath + "/trackImage/" + uuid);
                hashMap.putAll(trackService.updateTrackImage(uploadDto));
                if ("200".equals(hashMap.get("status"))) {
                    String trackImagePath = uploadDto.getUploadImagePath();
                    hashMap.put("imagePath",trackImagePath);
                }
            }
        }


        return hashMap;
    }

    @Override
    public Map<String, Object> updateMemberImage(UploadDto uploadDto) {

        Map<String,Object> hashMap = new HashMap<>();
        String uuid = generateUUID();
        hashMap.put("imagePath",null);

        //서버 이미지 저장
        if(uploadDto.getUploadImage() != null){
            Boolean isMemberImageUpload = fileService.uploadTrackImageFile(uploadDto.getUploadImage(),"/memberImage",uuid);

            if (isMemberImageUpload){
                uploadDto.setUploadImagePath(uploadPath + "/memberImage/" + uuid);
                hashMap.putAll(memberService.setMemberImage(uploadDto));
                if("200".equals(hashMap.get("status"))){
                    String memberImagePath = uploadDto.getUploadImagePath();
                    hashMap.put("imagePath",memberImagePath);
                }
            }

        }

        return hashMap;
    }


    private Long uploadImage(UploadDto uploadDto, String uuid) {
        // 마지막 트랙 id 얻기
        Long lastTrackId = trackService.getTrackLastId();

        // 앨범 이미지 업로드
        if (uploadDto.getUploadImage() != null) {
            uploadTrackImage(uploadDto, lastTrackId, uuid);
        }

        return lastTrackId;
    }


    private void uploadTrackImage(UploadDto uploadDto, Long lastTrackId, String uuid)  {
        String imagePath = uploadPath + "/trackImage/" + lastTrackId + "/" + uuid;
        uploadDto.setUploadImagePath(imagePath);
        fileService.uploadTrackImageFile(uploadDto.getUploadImage(), "/trackImage/" + lastTrackId, uuid);
    }

    private void uploadTrackFile(UploadDto uploadDto, Long lastTrackId, String uuid)  {
        String audioFilePath = "/" + lastTrackId + "/playList.m3u8";
        uploadDto.setUploadFilePath(audioFilePath);
        trackService.saveTrack(uploadDto);
        fileService.uploadTrackFile(uploadDto.getUploadFile(), "/track/", lastTrackId, uuid);
    }


    private Long uploadAlbumTrackFile(UploadDto uploadDto, Long lastTrackId, String uuid, int fileIdx)  {
        String audioFilePath =  "/" + lastTrackId + "/playList.m3u8";
        uploadDto.setUploadFilePath(audioFilePath);

        String trackNm = getUploadTrackFileNm(uploadDto.getUploadFileList().get(fileIdx));
        uploadDto.setTrackNm(trackNm);

        Map<String, Object> trackInfoMap = trackService.saveTrack(uploadDto);

        if (trackInfoMap.get("status").equals("200")) {
            fileService.uploadTrackFile(uploadDto.getUploadFileList().get(fileIdx), "/track/", lastTrackId, uuid);
        }

        return (Long) trackInfoMap.get("trackId");

    }

    private void saveAlbum(UploadDto uploadDto, List<Long> uploadTrackIdList) {
        PlayListRequestDto playListRequestDto = new PlayListRequestDto();
        playListRequestDto.setPlayListNm(uploadDto.getAlbumNm());
        playListRequestDto.setIsPlayListPrivacy(uploadDto.getTrackPrivacy());
        playListRequestDto.setLoginMemberId(uploadDto.getLoginMemberId());
        playListRequestDto.setIsAlbum(true);


        Map<String, Object> returnMap = playListService.newPlayList(playListRequestDto);
        if (returnMap.get("status").equals("200")) {
            playListRequestDto.setPlayListId((Long) returnMap.get("playListId"));

            for (Long trackId : uploadTrackIdList) {
                playListRequestDto.setTrackId(trackId);
                playListService.setPlayListTrack(playListRequestDto); //id랑 trackId
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
