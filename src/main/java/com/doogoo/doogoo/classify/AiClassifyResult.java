package com.doogoo.doogoo.classify;

public record AiClassifyResult(
        CategoryType category,
        String location,
        String target,
        Integer mileage
) {
}
