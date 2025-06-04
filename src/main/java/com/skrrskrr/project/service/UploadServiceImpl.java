package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.MemberResponseDto;
import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.dto.TrackResponseDto;
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

    @Value("${S3_UPLOAD_PATH}")
    private String s3UploadPath;

    private String generateUUID() {
        return String.valueOf(UUID.randomUUID());
    }

    @Override
    public void trackUpload(UploadDto uploadDto) {

        // 마지막 트랙 id 얻기
        Long lastTrackId = trackService.getTrackLastId();

        // 트랙 이미지 업로드
        uploadImage(uploadDto,lastTrackId);

        //트랙 업로드
        String audioPlayTime = uploadTrackFile(uploadDto.getUploadFile(), lastTrackId);
        String audioFilePath = s3UploadPath + "/track/" + lastTrackId + "/playlist.m3u8";
        uploadDto.setUploadFilePath(audioFilePath);
        uploadDto.setTrackTime(audioPlayTime);

        trackService.saveTrack(uploadDto);

    }

    @Override
    public void albumUpload(UploadDto uploadDto) {

        // 마지막 트랙 id 얻기
        Long lastTrackId = trackService.getTrackLastId();

        // 앨범 이미지 업로드
        uploadImage(uploadDto,lastTrackId);

        List<Long> uploadTrackIdList = new ArrayList<>();
        //앨범 트랙 업로드
        for (int i = 0; i < uploadDto.getUploadFileList().size(); i++) {
            String audioPlayTime = uploadTrackFile(uploadDto.getUploadFileList().get(i), lastTrackId);

            String trackNm = getUploadTrackFileNm(uploadDto.getUploadFileList().get(i));
            String audioFilePath = s3UploadPath +  "/" + lastTrackId + "/playList.m3u8";
            uploadDto.setUploadFilePath(audioFilePath);
            uploadDto.setTrackNm(trackNm);
            uploadDto.setTrackTime(audioPlayTime);

            trackService.saveTrack(uploadDto);

            uploadTrackIdList.add(lastTrackId);
            lastTrackId += 1;
        }

        // 앨범 정보 저장
        saveAlbum(uploadDto,uploadTrackIdList);
    }

    @Override
    public TrackResponseDto updateTrackImage(UploadDto uploadDto) {

        String trackImagePath = null;

        //이미지가 없을 경우 기본 이미지
        if (uploadDto.getUploadImage() != null) {

            Boolean isTrackImageUpload = fileService.uploadTrackImageFile(
                    uploadDto.getUploadImage(), "trackImage/" + uploadDto.getTrackId()
                    );

            if (isTrackImageUpload) {

                uploadDto.setUploadImagePath(s3UploadPath + "/trackImage/" + uploadDto.getTrackId());

                Boolean isSuccess = trackService.updateTrackImage(uploadDto);

                if (isSuccess) {
                    trackImagePath = uploadDto.getUploadImagePath();
                }
            }
        }
        return TrackResponseDto.builder()
                .trackImagePath(trackImagePath)
                .build();
    }

    @Override
    public MemberResponseDto updateMemberImage(UploadDto uploadDto) {

        String memberImagePath = null;

        //서버 이미지 저장
        if (uploadDto.getUploadImage() != null) {
            Boolean isMemberImageUpload = fileService.uploadTrackImageFile(
                    uploadDto.getUploadImage(),"memberImage/" + uploadDto.getLoginMemberId()
            );

            if (isMemberImageUpload) {
                uploadDto.setUploadImagePath(s3UploadPath + "/memberImage/" + uploadDto.getLoginMemberId());
                Boolean isSuccess = memberService.setMemberImage(uploadDto);
                if (isSuccess) {
                    memberImagePath = uploadDto.getUploadImagePath();
                }
            }
        }

        return MemberResponseDto.builder()
                .memberImagePath(memberImagePath)
                .build();
    }

    private void uploadImage(UploadDto uploadDto, Long lastTrackId) {
        // 앨범 이미지 업로드
        if (uploadDto.getUploadImage() != null) {
            uploadTrackImage(uploadDto, lastTrackId);
        }

    }

    private void uploadTrackImage(UploadDto uploadDto, Long lastTrackId)  {
        String imagePath = s3UploadPath + "/trackImage/" + lastTrackId;
        uploadDto.setUploadImagePath(imagePath);
        fileService.uploadTrackImageFile(
                uploadDto.getUploadImage(),
                "trackImage/" + lastTrackId
                );
    }

    private String uploadTrackFile(MultipartFile uploadTrackFile, Long lastTrackId)  {
        return fileService.uploadTrackFile(uploadTrackFile, lastTrackId);
    }


    private void saveAlbum(UploadDto uploadDto, List<Long> uploadTrackIdList) {
        PlayListRequestDto playListRequestDto = new PlayListRequestDto();
        playListRequestDto.setPlayListNm(uploadDto.getAlbumNm());
        playListRequestDto.setIsPlayListPrivacy(uploadDto.getIsTrackPrivacy());
        playListRequestDto.setLoginMemberId(uploadDto.getLoginMemberId());
        playListRequestDto.setIsAlbum(true);


        Long playListId = playListService.newPlayList(playListRequestDto);
        if (playListId != null) {
            playListRequestDto.setPlayListId(playListId);

            for (Long trackId : uploadTrackIdList) {
                playListRequestDto.setTrackId(trackId);
                playListService.setPlayListTrack(playListRequestDto);
            }
        }
    }



    private String getUploadTrackFileNm(MultipartFile trackFile){

        String originalFilename = trackFile.getOriginalFilename();

        if (originalFilename == null) {
            throw new IllegalStateException("originalFilename cannot be null.");
        }

        int dotIndex = originalFilename.lastIndexOf('.');

        return dotIndex != -1
                ? originalFilename.substring(0, dotIndex)
                : originalFilename;
    }



}
