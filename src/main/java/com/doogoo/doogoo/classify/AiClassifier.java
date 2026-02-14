package com.doogoo.doogoo.classify;

import com.doogoo.doogoo.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AiClassifier.class);

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
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder
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

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public AiClassifyResult classify(String title, String description) {
        try {
            String prompt = promptTemplate.formatted(title, description);

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
            log.warn("[{}] {}: {}", ErrorCode.AI_CLASSIFICATION_FAILED.getCode(), ErrorCode.AI_CLASSIFICATION_FAILED.getMessage(), e.getMessage(), e);
            return new AiClassifyResult(CategoryType.ETC, null, null, null);
        }
    }

    private AiClassifyResult parseResponse(String responseBody) {
        try {
            OpenAiResponse response = objectMapper.readValue(responseBody, OpenAiResponse.class);
            String content = response.choices().get(0).message().content().trim();

            RawClassifyResult raw = objectMapper.readValue(content, RawClassifyResult.class);
            return raw.toAiClassifyResult();
        } catch (Exception e) {
            log.warn("[{}] {}: {}", ErrorCode.AI_CLASSIFICATION_FAILED.getCode(), ErrorCode.AI_CLASSIFICATION_FAILED.getMessage(), e.getMessage(), e);
            return new AiClassifyResult(CategoryType.ETC, null, null, null);
        }
    }
}
