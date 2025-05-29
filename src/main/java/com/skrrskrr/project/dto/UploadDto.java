package com.skrrskrr.project.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UploadDto {

    private Long loginMemberId;

    private Long trackId;

    private String trackNm;

    private String albumNm;

    private String trackInfo;

    private String albumInfo;

    private String trackTime;

    private Long trackLikeCnt;

    private Long trackPlayCnt;

    private Long trackCategoryId;

    private Boolean isAlbum;

    private Boolean isTrackPrivacy;

    private String uploadFilePath;

    private String uploadImagePath;

    private MultipartFile uploadFile; // 음원 데이터

    private MultipartFile uploadImage; // 이미지 데이터

    private List<MultipartFile> uploadFileList; // 음원 데이터

}
