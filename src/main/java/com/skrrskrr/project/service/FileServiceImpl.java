package com.skrrskrr.project.service;

import com.skrrskrr.project.ffmpeg.FFmpegExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Log4j2
public class FileServiceImpl implements FileService{

    private final FFmpegExecutor fFmpegExecutor;

    @Autowired
    private S3Client s3Client;

    @Value("${S3_BUCKET_NAME}")
    private String s3BucketName;

    @Override
    public String uploadTrackFile(MultipartFile file, Long lastTrackId) {
        return fFmpegExecutor.convertAudioToM3U8(file, lastTrackId);
    }

    @Override
    public Boolean uploadTrackImageFile(MultipartFile file, String keyName, String uploadDate) {

        try {

            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(s3BucketName)
                    .prefix(keyName) // 해당 디렉토리 내 모든 파일 가져오기
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            listResponse.contents().forEach(obj -> {
                String key = obj.key(); // 디렉토리 내부의 파일 키

                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(s3BucketName)
                        .key(key)
                        .build());

                System.out.println("삭제 완료: " + key);
            });

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(s3BucketName)
                            .key(keyName + "/" + uploadDate)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            System.out.println("파일 업로드 완료! " + keyName);
            return true;
        } catch (IOException e) {
            System.err.println("파일 변환 중 오류 발생: " + e.getMessage());
            return false;
        }
    }
}
