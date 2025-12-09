package com.example.resume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class E5Client {

    @Value("${e5.python.command}")
    private String pythonCommand;   // .venv/Scripts/python.exe (상대 경로)

    @Value("${e5.script.path}")
    private String scriptPath;      // e5_modelling/main.py (상대 경로)

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * pdfFile: 스프링이 Temp 폴더에 저장한 PDF 파일
     * jobRole: "frontend" / "backend" / "uiux" 같은 직무 문자열
     */
    public E5Result analyzeResume(File pdfFile, String jobRole) {
        try {
            // 1) 현재 프로젝트 루트(Neurons-develop)의 절대 경로
            Path baseDir = Paths.get("").toAbsolutePath();

            // 2) 상대 경로로 들어온 pythonCommand, scriptPath를 절대 경로로 변환
            Path pythonPath = Paths.get(pythonCommand);
            if (!pythonPath.isAbsolute()) {
                pythonPath = baseDir.resolve(pythonPath).normalize();
            }

            Path script = Paths.get(scriptPath);
            if (!script.isAbsolute()) {
                script = baseDir.resolve(script).normalize();
            }

            System.out.println("[E5] baseDir    = " + baseDir);
            System.out.println("[E5] pythonPath = " + pythonPath);
            System.out.println("[E5] scriptPath = " + script);
            System.out.println("[E5] tempPdf    = " + pdfFile.getAbsolutePath());

            // 3) 프로세스 실행: python main.py <pdfPath> <jobRole>
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath.toString(),
                    script.toString(),
                    pdfFile.getAbsolutePath(),
                    jobRole
            );

            // 한글 로그 깨지지 않도록 UTF-8로 읽기
            pb.redirectErrorStream(true); // stderr -> stdout 합치기

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }

            int exitCode = process.waitFor();
            System.out.println("[E5] exitCode = " + exitCode);
            System.out.println("[E5] raw output = " + output);

            if (exitCode != 0) {
                throw new RuntimeException("E5 Python script failed. exitCode=" + exitCode);
            }

            // 4) main.py가 마지막에 JSON 한 줄을 찍는다고 가정
            String json = output.toString().trim();
            JsonNode root = objectMapper.readTree(json);

            int score = root.path("score").asInt();
            String keyword = root.path("keyword").asText();

            E5Result result = new E5Result();
            result.setScore(score);
            result.setKeyword(keyword);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("E5 분석 호출 실패: " + e.getMessage(), e);
        }
    }

    // E5 결과를 담는 간단한 DTO
    public static class E5Result {
        private int score;
        private String keyword;

        public int getScore() {
            return score;
        }
        public void setScore(int score) {
            this.score = score;
        }
        public String getKeyword() {
            return keyword;
        }
        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
    }
}
