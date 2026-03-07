# 🚀 배포 전 체크리스트

## 📋 수동 설정 사항 (배포 전 필수)

### 1️⃣ 환경 변수 설정 (`.env` 파일)
**파일 위치:** 프로젝트 루트 또는 배포 서버

```bash
# 필수 - 학사 일정 크롤링 활성화
SEJONG_PORTAL_ID=<실제_포털_아이디>
SEJONG_PORTAL_PASSWORD=<실제_포털_비밀번호>

# 선택적 - AI 분류 기능 사용 시
OPENAI_API_KEY=<OpenAI_API_KEY>

# 프로필 설정 (배포 시: prod)
SPRING_PROFILES_ACTIVE=prod
```

**⚠️ 중요:**
- `.env` 파일은 `.gitignore`에 등록되어 있어 Git에 커밋되지 않습니다
- 프로덕션 환경에서 반드시 실제 자격증명을 설정해야 합니다
- 없으면 학사 일정 크롤링이 실패합니다

---

### 2️⃣ 초기 학사 일정 데이터 수동 크롤링
**시기:** 최초 배포 후 첫 실행 시
**이유:** `crawlAcademicSchedule()` 크론은 **1월 1일**에만 실행되므로, 초기 배포 후 수동으로 실행해야 함

#### 방법 A: 관리자 엔드포인트 호출 (권장)
```bash
# 현재 연도의 학사 일정 크롤링
curl -X POST http://localhost:8080/admin/crawl/academic/current

# 현재 연도와 이전 연도 크롤링
curl -X POST http://localhost:8080/admin/crawl/academic/current-and-previous

# 특정 연도 크롤링
curl -X POST http://localhost:8080/admin/crawl/academic/2025
```

#### 방법 B: Spring Boot Actuator 사용
```bash
# 커스텀 엔드포인트를 actuator에 등록하면 사용 가능
curl -X POST http://localhost:8080/actuator/crawl-academic
```

#### 방법 C: 데이터베이스 수동 입력
학사 일정이 없으면 수동으로 INSERT
```sql
INSERT INTO academic_schedules (year, department, content, start_date, end_date)
VALUES (2025, '학부', '수강신청', '2025-08-20', '2025-08-24');
```

---

### 3️⃣ CORS 설정 변경
**파일:** `src/main/java/com/doogoo/doogoo/global/config/WebMvcConfig.java`
**변경 전:**
```java
.allowedOriginPatterns("*")  // 모든 도메인 허용 (개발용)
```

**변경 후:**
```java
.allowedOriginPatterns(
    "https://yourdomain.com",
    "https://www.yourdomain.com",
    "https://app.yourdomain.com"
)
```

**⚠️ 보안 주의:**
- `"*"` 는 개발/테스트 환경에서만 사용
- 프로덕션에서는 반드시 실제 프론트엔드 도메인으로 제한
- 여러 도메인 지원 시 모두 명시

---

## 🔄 자동으로 실행되는 크론 작업

| 크론 표현식 | 메서드 | 설명 | 주기 |
|----------|---------|------|------|
| `0 0 */6 * * *` | `regularCrawl()` | 두드림 공지 정기 크롤링 | **6시간마다** |
| `0 0 3 * * *` | `fullSync()` | 공개된 공지 전체 동기화 | **매일 03:00** |
| `0 0 2 1 1 *` | `crawlAcademicSchedule()` | 학사 일정 크롤링 | **1월 1일 02:00** |
| `0 0 4 * * *` | `cleanExpiredEvents()` | 만료된 공지 정리 | **매일 04:00** |

---

## ✅ 배포 전 검증 체크리스트

- [ ] `.env` 파일에 `SEJONG_PORTAL_PASSWORD` 설정 확인
- [ ] `SPRING_PROFILES_ACTIVE=prod` 설정 확인
- [ ] `WebMvcConfig.java`의 CORS 설정이 실제 도메인으로 변경됨
- [ ] 최초 배포 후 학사 일정 초기 크롤링 실행 완료
- [ ] 데이터베이스 백업 생성
- [ ] 로그 레벨이 적절하게 설정됨 (`INFO` 이상)

---

## 📝 로그 확인 포인트

배포 후 다음 로그를 통해 정상 동작 확인:

```
=== 정기 크롤링 시작 ===
상태 'scheduled' 크롤링 시작
상태 'open' 크롤링 시작
총 N개의 고유 공지 발견
=== 정기 크롤링 완료: 신규=X, 업데이트=Y, 마감=Z ===

=== 학사일정 크롤링 시작: year=2025 ===
=== 학사일정 크롤링 완료: year=2025, N건 ===
```

---

## 🆘 문제 해결

### Q: 학사 일정이 안 나타나요
**A:** `.env`에서 `SEJONG_PORTAL_PASSWORD` 확인 → 초기 크롤링 수동 실행 필요

### Q: CORS 에러가 나요
**A:** `WebMvcConfig.java`에서 프론트엔드 도메인 명시 확인

### Q: 크롤링이 너무 느려요
**A:** 서버 리소스 확인 및 크론 스케줄 조정 검토


