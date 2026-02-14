package com.doogoo.doogoo.classify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record RawClassifyResult(String category, String location, String target, Integer mileage) {

    AiClassifyResult toAiClassifyResult() {
        CategoryType type;
        try {
            type = CategoryType.valueOf(category != null ? category : "ETC");
        } catch (IllegalArgumentException e) {
            type = CategoryType.ETC;
        }

        return new AiClassifyResult(
                type,
                nullIfEmpty(location),
                nullIfEmpty(target),
                mileage
        );
    }

    private static String nullIfEmpty(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }
}
