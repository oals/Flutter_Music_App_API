package com.skrrskrr.project.ffmpeg;


import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class FFmpegExecutor {



    public void convertAudioToM3U8(String inputFilePath, String outputDir) {
        try {
            String command = String.format("ffmpeg -i %s -vn -acodec aac -b:a 128k -f" +
                            " hls -hls_time 10 -hls_list_size 0 -hls_segment_filename " +
                            "\"%s/output%%03d.ts\" %s/playlist.m3u8",
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



}
