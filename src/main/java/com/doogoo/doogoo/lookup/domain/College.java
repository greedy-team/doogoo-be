package com.doogoo.doogoo.lookup.domain;

public enum College {
    COL_HUMANITIES("col-humanities", "인문과학대학"),
    COL_SOCIAL("col-social", "사회과학대학"),
    COL_BUSINESS_ECON("col-business-econ", "경영경제대학"),
    COL_HOTEL_TOURISM("col-hotel-tourism", "호텔관광대학"),
    COL_NATURAL("col-natural", "자연과학대학"),
    COL_LIFE("col-life", "생명과학대학"),
    COL_AI("col-ai", "인공지능융합대학"),
    COL_ENGINEERING("col-engineering", "공과대학"),
    COL_ARTS_SPORTS("col-arts-sports", "예체능대학"),
    COL_DAEYANG_HUMANITY("col-daeyang-humanity", "대양휴머니티칼리지(교양대학)");

    private final String id;
    private final String displayName;

    College(String id, String displayName) {
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
