package com.skrrskrr.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {


    boolean uploadTrackFile(MultipartFile file, String dir, String trackNm);

    boolean uploadTrackImageFile(MultipartFile file,String dir, String imageFileNm);

}
