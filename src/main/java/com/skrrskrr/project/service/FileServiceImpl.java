package com.skrrskrr.project.service;

import com.skrrskrr.project.ffmpeg.FFmpegExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class FileServiceImpl implements FileService{

    private final FFmpegExecutor fFmpegExecutor;

    @Value("${upload.path}")
    private String uploadPath;


    @Override
    public boolean uploadTrackFile(MultipartFile file, String dir, String trackNm) {
        if (file.isEmpty()) {
            throw new RuntimeException("파일이 비어 있습니다.");
        }

        // 저장할 경로 설정
        String uploadDir = uploadPath + dir; // 원하는 경로로 변경
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
            fFmpegExecutor.convertAudioToM3U8(uploadDir + "/" + trackNm + fileType, uploadDir);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean uploadTrackImageFile(MultipartFile file, String dir,String imageFileNm) {
        if (file.isEmpty()) {
            throw new RuntimeException("파일이 비어 있습니다.");
        }

        // 저장할 경로 설정
        String uploadDir = uploadPath + dir; // 원하는 경로로 변경
        File uploadDirectory = new File(uploadDir);

        // 디렉토리가 없으면 생성
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // 파일 이름 생성 (예: 원래 파일 이름)
        String fileType = "";

        if (Objects.equals(file.getContentType(), "image/jpeg")) {
            fileType = ".jpg";
        } else if (Objects.equals(file.getContentType(), "image/png")) {
            fileType = ".png";
        } else if (Objects.equals(file.getContentType(), "image/svg+xml")) {
            fileType = ".svg";
        } else {
            fileType = ".jpg";
        }


        File destFile = new File(uploadDir, imageFileNm + fileType);

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
