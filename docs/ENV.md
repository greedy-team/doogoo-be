# 환경 변수

## 공통 (선택)

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `DATASOURCE_URL` | JDBC URL | `jdbc:h2:mem:doogoo;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` (로컬 H2) |
| `DATASOURCE_USERNAME` | DB 사용자명 | `sa` |
| `DATASOURCE_PASSWORD` | DB 비밀번호 | _(빈 문자열)_ |
| `PUBLIC_BASE_URL` | ICS 응답용 공개 base URL (미설정 시 요청 scheme/host로 생성) | _(빈 문자열)_ |

## 운영(Railway, prod 프로파일)

**필수**

| 변수 | 설명 | 예시 |
|------|------|------|
| `SPRING_PROFILES_ACTIVE` | 활성 프로파일 | `prod` |
| `DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://host:5432/dbname` |
| `DATASOURCE_USERNAME` | DB 사용자명 | Railway에서 제공 |
| `DATASOURCE_PASSWORD` | DB 비밀번호 | Railway에서 제공 |
| `PUBLIC_BASE_URL` | ICS URL용 공개 base (예: `https://api.example.com`) | 권장 |

- prod에서는 JPA `ddl-auto=validate` + Flyway로 스키마 적용.
- Railway에서 Postgres 추가 후 연결 URL/사용자/비밀번호를 위 변수로 설정하면 됨.
