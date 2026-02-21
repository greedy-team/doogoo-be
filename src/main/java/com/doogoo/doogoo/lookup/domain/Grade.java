package com.doogoo.doogoo.lookup.domain;

public enum Grade {
    FIRST("1", "1학년"),
    SECOND("2", "2학년"),
    THIRD("3", "3학년"),
    FOURTH("4", "4학년");

    private final String id;
    private final String displayName;

    Grade(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
}
