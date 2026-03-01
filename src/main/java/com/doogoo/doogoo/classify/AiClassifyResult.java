package com.doogoo.doogoo.classify;

import java.util.List;

public record AiClassifyResult(
        List<String> keywords
) {
    public static AiClassifyResult fallback() {
        return new AiClassifyResult(List.of("k_7"));
    }
}
