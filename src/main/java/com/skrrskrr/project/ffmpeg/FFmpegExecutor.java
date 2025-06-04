package com.skrrskrr.project.ffmpeg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FFmpegExecutor {

    @Autowired
    private S3Client s3Client;

    @Value("${S3_UPLOAD_PATH}")
    private String s3UploadPath;

    @Value("${S3_BUCKET_NAME}")
    private String s3BucketName;


    public String convertAudioToM3U8(MultipartFile multipartFile, Long lastTrackId) {

        Path workDir = Paths.get("/tmp/ffmpeg_work");
//        Path workDir = Paths.get("C:\\temp\\ffmpeg_work"); // 로컬 테스트 주소
        String trackPlayItem = null;

        try {
            Files.createDirectories(workDir);

            Path inputFile = workDir.resolve("input_file");
            try (InputStream in = multipartFile.getInputStream()) {
                Files.copy(in, inputFile, StandardCopyOption.REPLACE_EXISTING);
            }

            Path playlistFile = workDir.resolve("playlist.m3u8");
            String tsPattern = workDir.resolve("segment_%03d.ts").toString();

            List<String> command = new ArrayList<>();
            command.add("ffmpeg");
            command.add("-i");
            command.add(inputFile.toAbsolutePath().toString());
            command.add("-vn");
            command.add("-acodec");
            command.add("aac");
            command.add("-b:a");
            command.add("128k");
            command.add("-f");
            command.add("hls");
            command.add("-hls_time");
            command.add("10");
            command.add("-hls_list_size");
            command.add("0");
            command.add("-hls_segment_filename");
            command.add(tsPattern);
            command.add(playlistFile.toAbsolutePath().toString());

            ProcessBuilder pb = new ProcessBuilder(command);

            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                String durationString;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("Duration")) {
                        String[] parts = line.split(",")[0].split(" ");
                        if (parts.length > 1) {
                            durationString = parts[3];
                            if (durationString != null) {
                                String[] timeParts = durationString.split("\\.")[0].split(":");

                                if (timeParts.length == 3) {
                                    if (timeParts[0].equals("00")) {
                                        trackPlayItem = timeParts[1] + ":" + timeParts[2];
                                    } else {
                                        trackPlayItem = timeParts[0] + ":" + timeParts[1] + ":" + timeParts[2];
                                    }
                                }
                            }
                        }
                    }
                    System.out.println("[FFmpeg] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("FFmpeg 변환 실패: " + exitCode);
                return null;
            }

            String m3u8Content = Files.readString(playlistFile);

            List<String> modifiedLines = m3u8Content.lines()
                    .map(line -> {
                        if (line.trim().endsWith(".ts")) {
                            return s3UploadPath + "/track/" + lastTrackId + "/" + line.trim();
                        } else {
                            return line;
                        }
                    })
                    .collect(Collectors.toList());
            String updatedM3U8Content = String.join("\n", modifiedLines);

            byte[] m3u8Bytes = updatedM3U8Content.getBytes(StandardCharsets.UTF_8);
            try (ByteArrayInputStream m3u8Stream = new ByteArrayInputStream(m3u8Bytes)) {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(s3BucketName)
                                .key("track/" + lastTrackId + "/playlist.m3u8")
                                .contentType("application/vnd.apple.mpegurl")
                                .build(),
                        RequestBody.fromInputStream(m3u8Stream, m3u8Bytes.length)
                );
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(workDir, "segment_*.ts")) {
                for (Path tsFile : stream) {
                    try (InputStream tsStream = Files.newInputStream(tsFile)) {
                        s3Client.putObject(
                                PutObjectRequest.builder()
                                        .bucket(s3BucketName)
                                        .key("track/" + lastTrackId + "/" + tsFile.getFileName().toString())
                                        .contentType("video/mp2t")
                                        .build(),
                                RequestBody.fromInputStream(tsStream, Files.size(tsFile))
                        );
                    }
                    System.out.println(tsFile.getFileName() + ": 업로드 성공");
                }
            }
            deleteDirectoryRecursively(workDir.toFile());
            return trackPlayItem;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteDirectoryRecursively(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectoryRecursively(file);
            }
        }
        if (!dir.delete()) {
            System.err.println("파일 삭제 실패: " + dir.getAbsolutePath());
        }
    }
}
