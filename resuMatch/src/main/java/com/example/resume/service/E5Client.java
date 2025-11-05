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

    public E5AnalyzeResponse analyzeResume(MultipartFile file) {
        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<E5AnalyzeResponse> resp = restTemplate.exchange(
                    e5ApiUrl, HttpMethod.POST, requestEntity, E5AnalyzeResponse.class);

            return resp.getBody();
        } catch (Exception e) {
            throw new RuntimeException("E5 분석 호출 실패: " + e.getMessage(), e);
        }
    }
}
