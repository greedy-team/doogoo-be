package com.doogoo.doogoo.catalog.domain;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;

import java.util.Arrays;

public enum Keyword {
    COMPETITION("competition", "대회 및 학술제"),
    CAREER("career", "취창업"),
    VOLUNTEER("volunteer", "봉사·사회참여"),
    COUNSELING("counseling", "상담"),
    GLOBAL("global", "글로벌"),
    CAMPUS("campus", "캠퍼스"),
    ETC("etc", "기타");

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
