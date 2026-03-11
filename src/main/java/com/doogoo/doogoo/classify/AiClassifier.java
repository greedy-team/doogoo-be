package com.doogoo.doogoo.classify;

import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.common.log.JsonLog;
import com.doogoo.doogoo.common.log.LogDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class AiClassifier {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String apiUrl;
    private final String promptTemplate;

    public AiClassifier(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            @Value("${openai.url}") String apiUrl,
            @Value("classpath:prompts/classify.txt") Resource promptResource,
            ObjectMapper objectMapper
    ) {
        this.model = model;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;

        try {
            this.promptTemplate = new String(promptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트 파일 로드 실패", e);
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public AiClassifyResult classify(String title, String description, String department) {
        try {
            String dept = (department != null && !department.isBlank()) ? department : "정보 없음";
            String prompt = promptTemplate.formatted(title, description, dept);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "response_format", Map.of("type", "json_object")
            );

            String responseBody = restClient.post()
                    .uri(apiUrl)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseResponse(responseBody);
        } catch (Exception e) {
            JsonLog.warn(AiClassifier.class, new LogDto.ErrorLog(
                    "ai.classify.request.fail",
                    "openai-api",
                    ErrorCode.AI_CLASSIFICATION_FAILED.getStatus().value(),
                    ErrorCode.AI_CLASSIFICATION_FAILED.getCode(),
                    e.getMessage()
            ));
            return AiClassifyResult.fallback();
        }
    }

    private AiClassifyResult parseResponse(String responseBody) {
        try {
            OpenAiResponse response = objectMapper.readValue(responseBody, OpenAiResponse.class);
            String content = response.choices().get(0).message().content().trim();

            RawClassifyResult raw = objectMapper.readValue(content, RawClassifyResult.class);
            return raw.toAiClassifyResult();
        } catch (Exception e) {
            JsonLog.warn(AiClassifier.class, new LogDto.ErrorLog(
                    "ai.classify.parse.fail",
                    "openai-response",
                    ErrorCode.AI_CLASSIFICATION_FAILED.getStatus().value(),
                    ErrorCode.AI_CLASSIFICATION_FAILED.getCode(),
                    e.getMessage()
            ));
            return AiClassifyResult.fallback();
        }
    }
}
