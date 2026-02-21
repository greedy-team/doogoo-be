package com.doogoo.doogoo.lookup.domain;

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

    public String id() { return id; }
    public String displayName() { return displayName; }
}
