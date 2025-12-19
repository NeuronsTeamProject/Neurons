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
            String fileId = uploadFile(pdfBytes, fileName);
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
        body.add("purpose", "assistants");

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

        // ✅ content type은 input_text / input_file 로 써야 함
        String payload = """
        {
          "model": "%s",
          "input": [
            {
              "role": "user",
              "content": [
                {"type": "input_text", "text": "첨부한 이력서를 읽고, 5~7문장으로 총평을 한국어로 작성해줘. 강점 2가지와 보완점 1가지를 포함해줘."},
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

        JsonNode root = objectMapper.readTree(resp.getBody());

        // 1) output_text 필드가 있으면 우선 사용
        JsonNode outputText = root.path("output_text");
        if (outputText != null && !outputText.isMissingNode() && !outputText.isNull() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        // 2) output 배열 안에서 content[].type == "output_text" 를 찾아 text 뽑기
        JsonNode outputArr = root.path("output");
        if (outputArr.isArray()) {
            for (JsonNode msg : outputArr) {
                JsonNode contentArr = msg.path("content");
                if (!contentArr.isArray()) continue;

                for (JsonNode c : contentArr) {
                    String type = c.path("type").asText("");
                    if ("output_text".equals(type)) {
                        String text = c.path("text").asText("");
                        if (!text.isBlank()) return text;
                    }
                    // 혹시 구조가 text.value로 오는 경우 대비
                    JsonNode v = c.path("text").path("value");
                    if (!v.isMissingNode() && !v.isNull() && !v.asText().isBlank()) {
                        return v.asText();
                    }
                }
            }
        }

        // ❌ 절대 원본 JSON을 그대로 반환하지 말자(프론트에 저렇게 찍힘)
        return "GPT 응답에서 요약 텍스트를 추출하지 못했습니다.";
    }
}
