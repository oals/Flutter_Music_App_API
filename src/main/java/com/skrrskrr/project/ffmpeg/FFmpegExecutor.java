package com.skrrskrr.project.ffmpeg;


import org.springframework.stereotype.Service;
import org.tritonus.share.sampled.AudioUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FFmpegExecutor {

    private static int parseIntSafe(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0;  // 빈 문자열인 경우 0으로 처리
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;  // 파싱 실패 시 0 반환
        }
    }

    public String getAudioPlayTime(String inputFilePath) {
        try {

            String command = String.format("ffmpeg -i %s", inputFilePath);
            Process process = new ProcessBuilder(command.split(" ")).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            String durationString = null;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Duration")) {
                    String[] parts = line.split(",")[0].split(" ");

                    if (parts.length > 1) {
                        durationString = parts[3];
                        break;
                    }
                }
            }

            if (durationString != null) {
                String[] timeParts = durationString.split("\\.")[0].split(":");

                if (timeParts.length == 3) {
                    if (timeParts[0].equals("00")) {
                        return timeParts[1] + ":" + timeParts[2];
                    } else {
                        return timeParts[0] + ":" + timeParts[1] + ":" + timeParts[2];
                    }
                }
            }


            return null;  // Duration을 찾지 못했을 때

        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류 발생 시 처리
        }
    }


    public void convertAudioToM3U8(String inputFilePath, String outputDir) {
        try {

            String command = String.format(
                    "ffmpeg -i %s -vn -acodec aac -b:a 128k -f hls -hls_time 10 -hls_list_size 0 " +
                            "-hls_segment_filename \"%s/output%%03d.ts\" %s/playlist.m3u8",
                    inputFilePath, outputDir, outputDir);

            Process process = new ProcessBuilder(command.split(" ")).start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);  // 출력 내용을 처리
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);  // 에러 내용을 처리
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            outputThread.start();
            errorThread.start();

            int exitCode = process.waitFor();
            System.out.println("Process finished with exit code " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    public void modifyM3U8(String m3u8FilePath, String baseUrl) {

        try {
            // m3u8 파일을 읽기
            Path path = Paths.get(m3u8FilePath);
            List<String> lines = Files.readAllLines(path);

            // 각 줄을 수정하여 HTTP URL로 경로를 변경
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                // .ts 파일 경로가 나오면 HTTP URL로 교체
                if (line.endsWith(".ts")) {
                    String fileName = line.trim();
                    String newUrl = baseUrl + fileName;
                    lines.set(i, newUrl);
                }
            }

            // 수정된 내용을 다시 m3u8 파일에 작성
            Files.write(path, lines);
            System.out.println("m3u8 file updated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
