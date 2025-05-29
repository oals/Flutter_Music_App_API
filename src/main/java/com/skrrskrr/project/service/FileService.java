package com.skrrskrr.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String uploadTrackFile(MultipartFile file, String dir, Long lastTrackId , String trackNm);

    Boolean uploadTrackImageFile(MultipartFile file,String dir, String imageFileNm);

}
