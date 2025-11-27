package com.example.resume.service;

import com.example.resume.dto.E5AnalyzeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class E5Client {

    private final RestTemplate restTemplate;

    @Value("${E5_API_URL:http://localhost:8000/analyze}")
    private String e5ApiUrl;

    public E5AnalyzeResponse analyzeResume(MultipartFile file, String jobRole) {
        try {
            // (1) PDF를 ByteArrayResource 로 변환
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // (2) multipart/form-data body 생성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);                  // PDF 전송
            body.add("job_role", jobRole);               // 직무 전송 (필드명은 E5 서버와 동일해야 함)

            // (3) 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // (4) E5 서버 호출
            ResponseEntity<E5AnalyzeResponse> resp = restTemplate.exchange(
                    e5ApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    E5AnalyzeResponse.class
            );

            return resp.getBody();

        } catch (Exception e) {
            throw new RuntimeException("E5 분석 호출 실패: " + e.getMessage(), e);
        }
    }

    // 기존 방식 유지(필요하면)
    public E5AnalyzeResponse analyzeResume(MultipartFile file) {
        return analyzeResume(file, null);
    }
}
