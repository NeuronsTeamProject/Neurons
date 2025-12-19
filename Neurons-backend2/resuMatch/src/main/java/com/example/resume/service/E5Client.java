package com.example.resume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class E5Client {

    @Value("${e5.python.command}")
    private String pythonCommand;   // .venv/Scripts/python.exe (상대 경로 가능)

    @Value("${e5.script.path}")
    private String scriptPath;      // e5_modelling/main.py (상대 경로 가능)

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * pdfFile: 디스크에 존재하는 PDF 파일
     * jobRole: "frontend" / "backend" / "uiux" 같은 직무 문자열
     */
    public E5Result analyzeResume(File pdfFile, String jobRole) {
        try {
            // 1) 현재 실행 디렉토리 기준 절대 경로
            Path baseDir = Paths.get("").toAbsolutePath();

            // 2) pythonCommand, scriptPath 절대 경로화
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

            // ✅ 파이썬 출력 인코딩 강제 (윈도우에서 한글/로그 섞일 때 특히 도움)
            pb.environment().put("PYTHONIOENCODING", "UTF-8");

            // stderr 를 stdout으로 합치기 (로그 + JSON 한 번에 읽기)
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder rawOut = new StringBuilder();
            String jsonLine = null;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    rawOut.append(line).append('\n');

                    // ✅ JSON 라인 찾기: total_sum이 포함된 JSON만 잡는다 (로그가 섞여도 안전)
                    String t = line.trim();
                    if (t.startsWith("{") && t.contains("\"total_sum\"")) {
                        jsonLine = t;
                    }
                }
            }

            int exitCode = process.waitFor();

            System.out.println("[E5] exitCode = " + exitCode);
            System.out.println("[E5] raw output = " + rawOut);

            if (exitCode != 0) {
                throw new RuntimeException("E5 Python script failed. exitCode=" + exitCode);
            }

            if (jsonLine == null) {
                // 예전 방식처럼 { ... } 한 줄이라도 잡아보는 플랜B
                jsonLine = rawOut.toString().lines()
                        .map(String::trim)
                        .filter(s -> s.startsWith("{") && s.endsWith("}"))
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException("E5 출력에서 JSON 형식의 줄({ ... })을 찾지 못했습니다.")
                        );
            }

            System.out.println("[E5] json line = " + jsonLine);

            // 4) JSON 파싱
            JsonNode root = objectMapper.readTree(jsonLine);

            // ✅ 파이썬이 주는 키: total_sum, matches
            double totalSum = root.path("total_sum").asDouble(0.0);

            // 점수 정책:
            // - total_sum(45.0 같은 값)을 정수로 반올림해서 사용
            // - 너가 /100 스케일 원하면 여기서 변환하면 됨
            int score = (int) Math.round(totalSum);

            // ✅ 키워드: matches 배열에서 keyword들 뽑아서 중복 제거 후 ", "로 합치기
            Set<String> keywords = new LinkedHashSet<>();
            JsonNode matches = root.path("matches");
            if (matches.isArray()) {
                for (JsonNode m : matches) {
                    String kw = m.path("keyword").asText("").trim();
                    if (!kw.isBlank()) keywords.add(kw);
                }
            }

            String keywordString = keywords.stream().collect(Collectors.joining(", "));

            E5Result result = new E5Result();
            result.setScore(score);
            result.setKeyword(keywordString);

            System.out.println("[E5] parsed score   = " + score);
            System.out.println("[E5] parsed keyword = " + keywordString);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("E5 분석 호출 실패: " + e.getMessage(), e);
        }
    }

    // E5 결과 DTO
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
