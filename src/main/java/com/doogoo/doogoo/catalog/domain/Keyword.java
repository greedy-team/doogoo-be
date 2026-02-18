package com.doogoo.doogoo.catalog.domain;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;

import java.util.Arrays;

public enum Keyword {
    K_1("k_1", "대회, 학술제"),
    K_2("k_2", "취창업");

    private final String id;
    private final String displayName;

    Keyword(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public static Keyword fromId(String id) {
        return Arrays.stream(values())
                .filter(k -> k.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT));
    }
}
