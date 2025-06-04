package com.skrrskrr.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String uploadTrackFile(MultipartFile file, Long lastTrackId );

    Boolean uploadTrackImageFile(MultipartFile file,String dir);

}
