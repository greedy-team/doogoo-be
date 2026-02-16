package com.doogoo.doogoo.lookup.domain;

public enum Keyword {
    K_1("k_1", "대회, 학술제"),
    K_2("k_2", "취창업");

    private final String id;
    private final String displayName;

    Keyword(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
}
