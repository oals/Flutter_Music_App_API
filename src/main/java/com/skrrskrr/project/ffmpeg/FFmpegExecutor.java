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

    public long getAudioDuration(String inputFilePath) {
        try {
            // ffmpeg 명령어로 미디어 파일의 메타데이터를 확인
            String command = String.format("ffmpeg -i %s", inputFilePath);
            Process process = new ProcessBuilder(command.split(" ")).start();

            // ffmpeg의 stderr에서 duration을 찾음
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            long duration = -1;

            // 출력에서 "Duration" 부분을 찾아서 시간을 추출
            while ((line = reader.readLine()) != null) {
                if (line.contains("Duration")) {
                    // Duration: 00:03:40.05 형태의 라인에서 시간을 추출
                    String[] parts = line.split(",")[0].split(" ");

                    // parts 배열이 정상적으로 분리되었는지 확인하고 접근
                    if (parts.length > 1) {
                        String timeString = parts[3];  // "00:03:40.05" 부분
                        String[] timeParts = timeString.split(":");  // ":"로 나누기

                        // timeParts 배열의 크기가 3인지 확인 (시, 분, 초)
                        if (timeParts.length == 3) {
                            // 각 부분을 파싱하여 총 초 단위로 계산
                            int hours = parseIntSafe(timeParts[0]); // 시간
                            int minutes = parseIntSafe(timeParts[1]); // 분
                            int seconds = parseIntSafe(timeParts[2].split("\\.")[0]);  // 초 (밀리초는 제외)

                            // 총 시간을 초 단위로 계산
                            duration = hours * 3600 + minutes * 60 + seconds;
                            break;  // 계산 후 종료
                        }
                    }
                }
            }

            return duration; // 초 단위로 반환

        } catch (Exception e) {
            e.printStackTrace();
            return -1; // 오류 발생 시 -1 반환
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
