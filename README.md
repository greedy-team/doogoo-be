# 두구두구 (DoogooDoogoo) Backend

<div align="center">

### 세종대 맞춤형 두드림 · 학사일정 ICS 구독 백엔드

세종대학교 **두드림** 비교과 공지와 **학사일정** 데이터를 수집·가공하고,<br>
학과·키워드·학년 조건이 반영된 개인화 `.ics` 구독 URL을 발급하는 Spring Boot 백엔드 서버

<br>

![Java](https://img.shields.io/badge/Java_17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-59666C?style=flat-square&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI_3-6BA539?style=flat-square&logo=swagger&logoColor=white)
![Caffeine](https://img.shields.io/badge/Caffeine_Cache-8B5A2B?style=flat-square)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white)

</div>

---

## 💡 기획 배경

> **두드림**과 **학사일정**은 필요한 정보가 흩어져 있고, 사용자가 직접 찾아보지 않으면 신청·운영 일정을 놓치기 쉽습니다.

| 문제 | 설명 |
|------|------|
| 정보 분산 | 학사일정과 두드림 공지가 서로 다른 성격으로 흩어져 있음 |
| 지속 확인 부담 | 사용자가 주기적으로 사이트를 직접 방문해야 함 |
| 개인화 부족 | 학교에서 제공하는 기본 일정은 개인 관심사 중심 필터링이 어려움 |
| 수동 등록 번거로움 | 캘린더 앱에 직접 입력하거나 복사해야 하는 불편 |

**목표:** 공지 데이터를 자동 수집하고, 사용자 조건이 반영된 개인화 ICS 구독 URL을 발급해 캘린더 앱에서 자동 동기화되도록 지원

---

## 🚀 요청 흐름

```
① 클라이언트 요청   학사/두드림 공지 조회 또는 ICS 발급 요청
       ↓
② 백엔드 처리      필터 검증 · 조회 · 구독 토큰 생성
       ↓
③ ICS URL 발급     고유 token 기반 .ics 주소 생성
       ↓
④ 캘린더 구독      사용자가 구글 · 애플 · 아웃룩에 등록
       ↓
⑤ ICS 조회         캘린더 앱이 /cal/{token}.ics 호출
       ↓
⑥ 자동 동기화      캐시된 공지/학사 데이터를 기반으로 최신 일정 반영
```

---

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 🎓 학사일정 API | 학년 정보가 포함된 학사일정 목록 조회 |
| 🌟 두드림 공지 API | 두드림 OPEN 공지 목록 조회 |
| 🔗 ICS 구독 URL 발급 | 학사/두드림 필터 조건을 token 기반 구독 URL로 변환 |
| 📆 ICS 캘린더 생성 | `/cal/{token}.ics` 요청 시 동적 ICS 본문 렌더링 |
| 🕷 크롤링 자동화 | 두드림 공지 및 학사일정 자동 수집/동기화 |
| 🤖 AI 분류/요약 | 두드림 상세 공지를 학과·키워드 기준으로 분류하고 요약 생성 |
| ⚡ 캐시 최적화 | 구독 토큰/ICS/공지 목록을 Caffeine으로 캐싱 |
| 📊 구조화 로그 | JSON 로그 기반 운영 로그 수집 및 모니터링 대응 |

<details>
<summary><b>백엔드 운영 기능 펼치기</b></summary>

| 기능 | 설명 |
|------|------|
| 🛠 관리자 크롤링 API | 특정 연도 학사일정 또는 두드림 크롤링 수동 실행 |
| 🔄 스케줄 배치 | 두드림 정기 크롤링, 전체 동기화, 학사일정 크롤링, 만료 이벤트 정리 |
| 🧹 구독 정리 | 비활성 구독 토큰 cleanup |
| 🧪 Swagger/OpenAPI | 운영/로컬 서버 문서화 및 테스트 지원 |

</details>

---

## 🛠 기술 스택

**Backend**

![Java](https://img.shields.io/badge/Java_17-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Web](https://img.shields.io/badge/Spring_Web-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-59666C?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![H2](https://img.shields.io/badge/H2-003B57?style=for-the-badge)
![OpenAPI](https://img.shields.io/badge/Springdoc_OpenAPI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Caffeine](https://img.shields.io/badge/Caffeine-8B5A2B?style=for-the-badge)
![jsoup](https://img.shields.io/badge/jsoup-4E8C2E?style=for-the-badge)

**Infra / Ops**

![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)

---

## 📁 프로젝트 구조

<details>
<summary><b>패키지 구조 펼치기</b></summary>

```text
src/main/java/com/doogoo/doogoo
├── academic/                      # 학사일정 조회·동기화
│   ├── api/                       # 학사일정 API, 요청/응답 DTO
│   ├── application/               # 학사 조회/연도별 동기화 서비스
│   ├── domain/                    # AcademicSchedule 엔티티 및 DTO
│   └── infrastructure/            # AcademicScheduleRepository
│
├── calendar/                      # ICS 캘린더 조회
│   ├── api/                       # /cal/{token}.ics 엔드포인트
│   └── application/               # ICS 렌더링, 캐시, 토큰 처리
│
├── dodream/                       # 두드림 공지 조회·동기화
│   ├── api/                       # 두드림 API, 요청/응답 DTO
│   ├── application/               # 공지 조회/저장/업데이트/마감 처리
│   ├── domain/                    # Event 엔티티, 상태, DTO
│   └── infrastructure/            # EventRepository
│
├── subscription/                  # 구독 토큰 발급·조회·정리
│   ├── application/               # 구독 발급/조회/cleanup 서비스
│   ├── domain/                    # Subscription 엔티티 및 관련 enum
│   └── infrastructure/            # SubscriptionRepository
│
├── lookup/                        # 학년·학과·키워드 조회
│   ├── api/
│   ├── application/
│   └── domain/
│
├── crawl/                         # 두드림/학사 크롤러, 파서, 스케줄러
├── classify/                      # AI 분류/요약 연동
├── common/                        # 에러 처리, 로그, 유틸리티
├── global/                        # 관리자 API, 공통 설정, 캐시, CORS, OpenAPI
└── DoogooApplication.java         # Spring Boot 진입점
```

```text
src/main/resources
├── application.yaml               # 공통 설정
├── application-prod.yaml          # 운영 DB / CORS 설정
├── application-test.yaml          # 테스트 설정
├── application-dev.yml            # 개발 설정
├── logback-spring.xml             # 구조화 로그 설정
└── prompts/                       # AI 분류/요약 프롬프트
```

```text
src/test/java/com/doogoo/doogoo
├── academic/api/                  # 학사 API 테스트
├── calendar/api/                  # ICS 조회 테스트
├── calendar/application/          # ICS 캐시 테스트
├── dodream/api/                   # 두드림 API 테스트
├── crawl/                         # 크롤링 파서 테스트
└── subscription/domain/           # 구독 도메인 테스트
```

</details>

---

## 🔌 API

백엔드 서버: `https://www.sejongdoogoo-api.com`

Swagger UI:
- 운영: `https://www.sejongdoogoo-api.com/swagger-ui/index.html`
- 로컬: `http://localhost:8080/swagger-ui/index.html`

### Public API

| Method | Endpoint | 설명 |
|:------:|----------|------|
| `GET` | `/api/grades` | 학년 목록 조회 |
| `GET` | `/api/colleges` | 단과대·학과 목록 조회 |
| `GET` | `/api/keywords` | 두드림 키워드 목록 조회 |
| `GET` | `/api/academic/notices` | 학사일정 목록 조회 |
| `GET` | `/api/dodream/notices` | 두드림 공지 목록 조회 |
| `POST` | `/api/academic/ics` | 학사일정 ICS 구독 URL 발급 |
| `POST` | `/api/dodream/ics` | 두드림 ICS 구독 URL 발급 |
| `GET` | `/cal/{token}.ics` | 구독 토큰 기반 ICS 캘린더 조회 |

### Admin API

| Method | Endpoint | 설명 |
|:------:|----------|------|
| `POST` | `/api/admin/crawl/academic/{year}` | 특정 연도 학사일정 크롤링 |
| `POST` | `/api/admin/crawl/academic/current` | 현재 연도 학사일정 크롤링 |
| `POST` | `/api/admin/crawl/academic/current-and-previous` | 현재·이전 연도 학사일정 크롤링 |
| `POST` | `/api/admin/crawl/dodream` | 두드림 정기 크롤링 수동 실행 |

### Health Check

| Method | Endpoint | 설명 |
|:------:|----------|------|
| `GET` | `/actuator/health` | 서버 상태 확인 |

---

## 🔄 배치 및 동기화

| 작업 | 크론 | 설명 |
|------|------|------|
| `regularCrawl()` | `0 0 0 */2 * *` | 두드림 정기 크롤링 |
| `fullSync()` | `0 0 3 * * *` | open 이벤트 상세 동기화 |
| `crawlAcademicSchedule()` | `0 0 2 1 1 *` | 연 1회 학사일정 크롤링 |
| `cleanExpiredEvents()` | `0 0 4 * * *` | 만료 이벤트 정리 |
| `deleteSubscription()` | `0 0 1 * * MON` | 비활성 구독 정리 |

---

## ⚙️ 실행 방법

### 1. 환경 변수 준비

프로젝트 루트의 `.env.example`를 참고해 다음 값을 준비합니다.

```bash
SEJONG_PORTAL_ID=your_portal_id
SEJONG_PORTAL_PASSWORD=your_portal_password
OPENAI_API_KEY=sk-...
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://localhost:5432/doogoo
DB_USERNAME=doogoo_user
DB_PASSWORD=your_db_password
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### 2. 로컬 실행

기본 프로필은 `dev`입니다.

```bash
./gradlew bootRun
```

테스트 실행:

```bash
./gradlew test
```

JAR 빌드:

```bash
./gradlew bootJar
```

### 3. 운영 설정 참고

- 운영 프로필: `prod`
- DB: PostgreSQL
- JPA DDL: `update`
- CORS: `CORS_ALLOWED_ORIGINS` 기반
- 공개 서버 정보: OpenAPI 설정에 production server 등록

---

## 🧠 백엔드 처리 포인트

| 항목 | 설명 |
|------|------|
| 토큰 발급 | Base62 12자리 토큰 생성 후 `subscriptions` 저장 |
| ICS 렌더링 | `token -> Subscription -> 필터 조건 -> ICS 본문 생성` |
| 캐시 | `icsCache`, `tokenCache`, 공지 목록 캐시 사용 |
| 동시성 제어 | ICS 렌더링용 `Semaphore(10)` 사용 |
| 로그 | `logback-spring.xml` 기반 JSON 구조화 로그 |
| 에러 처리 | `GlobalExceptionHandler`에서 API/ICS 요청 공통 처리 |

---

## 🧪 테스트

현재 포함된 테스트 예시:

- `AcademicControllerTest`
- `DoDreamControllerTest`
- `CalendarControllerTest`
- `IcsCacheTest`
- `AcademicParserTest`
- `DodreamParserTest`
- `SubscriptionTest`

핵심 검증 범위:

- 공지 조회 API 응답
- ICS 발급 및 조회
- 구독 토큰 처리
- 캐시 동작
- 크롤링 파싱 로직

---

## 🗺 향후 계획

- 두드림 필터를 DB 조건으로 더 직접 반영해 조회 비용 최적화
- cleanup 쿼리와 인덱스 전략 고도화
- 운영 로그/Grafana 기반 모니터링 강화
- 배치 상태 및 크롤링 결과 가시성 개선
- 배포/운영 문서와 백엔드 README 연동 강화

---
