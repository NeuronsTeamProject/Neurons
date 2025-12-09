package com.example.resume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GptClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${OPENAI_API_KEY:}")
    private String openAiApiKey;

    @Value("${OPENAI_BASE_URL:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Value("${OPENAI_MODEL:gpt-4.1-mini}")
    private String openAiModel;

    public String summarizePdf(byte[] pdfBytes, String fileName) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 필요합니다.");
        }
        try {
            // 1) 파일 업로드
            String fileId = uploadFile(pdfBytes, fileName);

            // 2) Responses API로 총평 생성 (파일 그대로 참조)
            return createResponseWithFile(fileId);
        } catch (Exception e) {
            throw new RuntimeException("GPT 총평 생성 실패: " + e.getMessage(), e);
        }
    }

    private String uploadFile(byte[] bytes, String fileName) throws Exception {
        String url = openAiBaseUrl + "/files";

        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() { return fileName; }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("purpose", "assistants"); // 파일을 그대로 참조하기 위한 목적

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, req, String.class);

        JsonNode json = objectMapper.readTree(resp.getBody());
        return json.get("id").asText();
    }

    private String createResponseWithFile(String fileId) throws Exception {
        String url = openAiBaseUrl + "/responses";

        // 최신 Responses 포맷(파일 첨부 참조) 예시 페이로드
        String payload = """
        {
          "model": "%s",
          "input": [
            {
              "role": "user",
              "content": [
                {"type": "text", "text": "첨부한 이력서를 읽고, 5~7문장으로 총평을 한국어로 작성해줘. 강점 2가지와 보완점 1가지를 포함해줘."},
                {"type": "input_file", "file_id": "%s"}
              ]
            }
          ]
        }
        """.formatted(openAiModel, fileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<String> req = new HttpEntity<>(payload, headers);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, req, String.class);

        // 응답 본문에서 텍스트 추출 (모델/버전에 따라 경로 다를 수 있어 안전파싱)
        JsonNode root = objectMapper.readTree(resp.getBody());
        JsonNode output = root.path("output_text");
        if (!output.isMissingNode() && !output.isNull()) {
            return output.asText();
        }
        // 대안: 첫 메시지 content에서 텍스트 수집
        JsonNode content = root.path("output").path(0).path("content");
        if (content.isArray() && content.size() > 0) {
            // content[0].text.value 형태를 시도
            JsonNode t = content.get(0).path("text").path("value");
            if (!t.isMissingNode()) return t.asText();
        }
        // 실패 시 원문 반환
        return resp.getBody();
    }
}
