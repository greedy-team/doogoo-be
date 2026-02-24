package com.doogoo.doogoo.lookup.domain;

public enum Department {
    DEPT_KOREAN("dept-korean", "국어국문학과", College.COL_HUMANITIES),
    DEPT_INTERNATIONAL("dept-international", "국제학부", College.COL_HUMANITIES),
    DEPT_HISTORY("dept-history", "역사학과", College.COL_HUMANITIES),
    DEPT_EDUCATION("dept-education", "교육학과", College.COL_HUMANITIES),
    DEPT_GLOBAL_TALENT("dept-global-talent", "글로벌인재학부", College.COL_HUMANITIES),
    DEPT_PUBLIC_ADMIN("dept-public-admin", "행정학과", College.COL_SOCIAL),
    DEPT_MEDIA_COMM("dept-media-comm", "미디어커뮤니케이션학과", College.COL_SOCIAL),
    DEPT_LAW("dept-law", "법학과", College.COL_SOCIAL),
    DEPT_BUSINESS("dept-business", "경영학부", College.COL_BUSINESS_ECON),
    DEPT_ECONOMICS("dept-economics", "경제학과", College.COL_BUSINESS_ECON),
    DEPT_HOTEL_TOURISM_FOOD("dept-hotel-tourism-food", "호텔관광외식경영학부", College.COL_HOTEL_TOURISM),
    DEPT_HOTEL_FOOD_FRANCHISE("dept-hotel-food-franchise", "호텔외식관광프랜차이즈경영학과", College.COL_HOTEL_TOURISM),
    DEPT_CULINARY_SERVICE("dept-culinary-service", "조리서비스경영학과", College.COL_HOTEL_TOURISM),
    DEPT_MATH_STATS("dept-math-stats", "수학통계학과", College.COL_NATURAL),
    DEPT_PHYSICS_ASTRO("dept-physics-astro", "물리천문학과", College.COL_NATURAL),
    DEPT_CHEMISTRY("dept-chemistry", "화학과", College.COL_NATURAL),
    DEPT_LIFE_SYSTEM("dept-life-system", "생명시스템학부", College.COL_LIFE),
    DEPT_SMART_LIFE_INDUSTRY("dept-smart-life-industry", "스마트생명산업융합학과", College.COL_LIFE),
    DEPT_AI_ELECTRONICS("dept-ai-electronics", "AI융합전자공학과", College.COL_AI),
    DEPT_SEMICONDUCTOR_SYSTEM("dept-semiconductor-system", "반도체시스템공학과", College.COL_AI),
    DEPT_CSE("dept-cse", "컴퓨터공학과", College.COL_AI),
    DEPT_INFORMATION_SECURITY("dept-information-security", "정보보호학과", College.COL_AI),
    DEPT_QUANTUM_INTELLIGENCE_INFO("dept-quantum-intelligence-info", "양자지능정보학과", College.COL_AI),
    DEPT_CREATIVE_SOFTWARE("dept-creative-software", "창의소프트학부", College.COL_AI),
    DEPT_CYBER_DEFENSE("dept-cyber-defense", "사이버국방학과", College.COL_AI),
    DEPT_DEFENSE_AI_ROBOT_MARINE("dept-defense-ai-robot-marine", "국방AI로봇융합공학과", College.COL_AI),
    DEPT_ADVANCED_CONVERGENCE("dept-advanced-convergence", "첨단융합계열", College.COL_AI),
    DEPT_AI_DATA_SCIENCE("dept-ai-data-science", "인공지능데이터사이언스학과", College.COL_AI),
    DEPT_AI_ROBOT("dept-ai-robot", "AI로봇학과", College.COL_AI),
    DEPT_INTELLIGENT_INFO_CONVERGENCE("dept-intelligent-info-convergence", "지능정보융합학과", College.COL_AI),
    DEPT_CONTENT_SOFTWARE("dept-content-software", "콘텐츠소프트웨어학과", College.COL_AI),
    DEPT_ARCHITECTURE_ENGINEERING("dept-architecture-engineering", "건축공학과", College.COL_ENGINEERING),
    DEPT_ARCHITECTURE("dept-architecture", "건축학과", College.COL_ENGINEERING),
    DEPT_CIVIL_ENVIRONMENT("dept-civil-environment", "건설환경공학과", College.COL_ENGINEERING),
    DEPT_ENVIRONMENT_CONVERGENCE("dept-environment-convergence", "환경융합공학과", College.COL_ENGINEERING),
    DEPT_ENERGY_RESOURCE("dept-energy-resource", "에너지자원공학과", College.COL_ENGINEERING),
    DEPT_MECHANICAL("dept-mechanical", "기계공학과", College.COL_ENGINEERING),
    DEPT_AEROSPACE_SYSTEM("dept-aerospace-system", "우주항공시스템공학부", College.COL_ENGINEERING),
    DEPT_NANO_MATERIALS("dept-nano-materials", "나노신소재공학과", College.COL_ENGINEERING),
    DEPT_QUANTUM_NUCLEAR("dept-quantum-nuclear", "양자원자력공학과", College.COL_ENGINEERING),
    DEPT_DEFENSE_AI_NAVY("dept-defense-ai-navy", "국방AI융합시스템공학과", College.COL_ENGINEERING),
    DEPT_PAINTING("dept-painting", "회화과", College.COL_ARTS_SPORTS),
    DEPT_FASHION_DESIGN("dept-fashion-design", "패션디자인학과", College.COL_ARTS_SPORTS),
    DEPT_MUSIC("dept-music", "음악과", College.COL_ARTS_SPORTS),
    DEPT_PHYSICAL_EDUCATION("dept-physical-education", "체육학과", College.COL_ARTS_SPORTS),
    DEPT_DANCE("dept-dance", "무용과", College.COL_ARTS_SPORTS),
    DEPT_FILM_ARTS("dept-film-arts", "영화예술학과", College.COL_ARTS_SPORTS),
    DEPT_LIBERAL_STUDIES("dept-liberal-studies", "자유전공학부", College.COL_DAEYANG_HUMANITY);

    private final String id;
    private final String displayName;
    private final College college;

    Department(String id, String displayName, College college) {
        this.id = id;
        this.displayName = displayName;
        this.college = college;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public College college() {
        return college;
    }
}
