package com.skrrskrr.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {


    boolean uploadTrackFile(MultipartFile file, String dir, Long lastTrackId , String trackNm);

    boolean uploadTrackImageFile(MultipartFile file,String dir, String imageFileNm);

}
