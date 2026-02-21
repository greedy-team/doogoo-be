package com.doogoo.doogoo.catalog.domain;

public enum Department {
    DEP_1("dep_1", "컴퓨터공학과"),
    DEP_2("dep_2", "소프트웨어학과");

    private final String id;
    private final String displayName;

    Department(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

}
