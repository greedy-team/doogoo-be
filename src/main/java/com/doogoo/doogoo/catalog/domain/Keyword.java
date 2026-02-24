package com.doogoo.doogoo.catalog.domain;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;

import java.util.Arrays;

public enum Keyword {
    COMPETITION("competition", "학술/연구", "경진대회, 공모전, 학술행사", "competition"),
    CAREER("career", "취창업", "취업, 창업, 진로 관련 행사", "career"),
    VOLUNTEER("volunteer", "봉사·사회참여", "봉사, 사회참여 활동", "volunteer"),
    COUNSELING("counseling", "상담", "상담, 심리 지원", "counseling"),
    GLOBAL("global", "글로벌", "해외 교류, 국제 프로그램", "global"),
    CAMPUS("campus", "캠퍼스", "캠퍼스 생활, 동아리 등", "campus"),
    ETC("etc", "기타", "기타 행사", "etc");

    private final String id;
    private final String displayName;
    private final String description;
    private final String icon;

    Keyword(String id, String displayName, String description, String icon) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public String icon() {
        return icon;
    }

    public static Keyword fromId(String id) {
        return Arrays.stream(values())
                .filter(k -> k.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT));
    }
}
