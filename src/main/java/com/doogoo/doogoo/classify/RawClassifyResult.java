package com.doogoo.doogoo.classify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record RawClassifyResult(List<String> keywords) {

    AiClassifyResult toAiClassifyResult() {
        if (keywords == null || keywords.isEmpty()) {
            return AiClassifyResult.fallback();
        }
        return new AiClassifyResult(keywords);
    }
}
