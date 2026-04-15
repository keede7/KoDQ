# Architecture Decision Records

## 철학

MVP 속도 최우선. 인증·저장소 없이 분석 기능 하나에 집중. 외부 의존성은 꼭 필요한 것만.

---

### ADR-001: 백엔드 프레임워크 — Ktor 선택

- **Status**: Accepted (2026-04-14)
- **결정**: Spring Boot 대신 Ktor (Kotlin 경량 프레임워크) 사용
- **이유**: 단일 API 엔드포인트(`POST /api/analyze`)만 필요한 구조에서 Spring Boot의 무게가 불필요함. Kotlin 네이티브로 코루틴 기반 비동기 처리가 자연스럽고, Fat JAR 빌드 및 Docker 배포가 간단함.
- **트레이드오프**: Spring 생태계(Spring Security, JPA 등)를 사용할 수 없음. 인증·DB가 필요해지면 마이그레이션 비용 발생.

---

### ADR-002: 모바일 프레임워크 — Flutter 선택

- **Status**: Accepted (2026-04-14)
- **결정**: React Native, Swift 네이티브 대신 Flutter 사용
- **이유**: iOS 우선이지만 추후 Android 확장을 고려. Dart 단일 코드베이스로 양 플랫폼 대응 가능. 1인 개발 환경에서 유지보수 부담 최소화.
- **트레이드오프**: iOS 네이티브 대비 카메라·갤러리 API 접근이 플러그인(`image_picker`) 의존적. 세밀한 iOS UI 커스터마이징에 제약.

---

### ADR-003: AI 분석 — Claude Vision API 선택

- **Status**: Accepted (2026-04-14)
- **결정**: OpenAI GPT-4o 대신 Anthropic Claude Vision (`claude-sonnet-4-6`) 사용
- **이유**: 한국어 지시문에 대한 JSON 구조화 응답 품질. 의상 색상·재질·스타일 조합 분석에 필요한 멀티모달 이미지 이해 능력.
- **트레이드오프**: 응답이 마크다운 코드블록으로 감싸지는 경우가 있어 서버에서 파싱 전처리 필요. API 비용이 요청당 발생 (캐싱 없음).

---

### ADR-004: 이미지 전송 방식 — multipart 선택

- **Status**: Accepted (2026-04-14)
- **결정**: Base64 JSON body 대신 `multipart/form-data`로 이미지 전송
- **이유**: 모바일에서 Base64 인코딩 시 원본 대비 약 33% 크기 증가 + 메모리 부담. multipart는 raw 바이트 전송 후 서버에서 인코딩하므로 클라이언트 부담 경감.
- **트레이드오프**: 서버에서 multipart 파싱 및 mediaType 감지 처리 필요.

---

### ADR-005: 에러 응답 전략 — 구조화된 JSON 통일

- **Status**: Accepted (2026-04-14)
- **결정**: 모든 에러 응답을 `{"error": "<메시지>"}` 형태의 JSON으로 통일
- **이유**: 클라이언트(Flutter)가 에러 메시지를 파싱해 사용자에게 표시할 수 있음. 빈 body나 plain text 에러는 클라이언트에서 파싱 오류 유발.
- **에러 케이스별 HTTP 상태코드**:
  - `400 Bad Request` — 이미지 파트 누락 등 클라이언트 입력 오류
  - `500 Internal Server Error` — Claude API 오류, JSON 파싱 실패 등 서버 내부 오류
- **트레이드오프**: 에러 코드 체계(enum) 없음. MVP 단계에서는 메시지 문자열로만 구분하므로, 클라이언트가 에러 종류별 분기 처리를 하기 어려움. 추후 `{"error": "...", "code": "PARSE_FAILED"}` 형태로 확장 가능.

---

### ADR-006: 이미지 포맷 감지 — magic bytes 방식 선택

- **Status**: Accepted (2026-04-14)
- **결정**: Content-Type 헤더 대신 파일 시그니처(magic bytes)로 이미지 포맷 감지 (`detectMediaType` 함수, `StyleRoutes.kt`)
- **이유**: Content-Type 헤더는 클라이언트가 잘못 설정하거나 누락할 수 있어 신뢰 불가. 파일 바이트 헤더로 감지하면 실제 포맷을 정확히 판별할 수 있음. Claude API는 `media_type` 필드가 정확해야 올바른 이미지 해석이 가능.
- **지원 포맷**: JPEG(`FF D8`), PNG(`89 50 4E`), GIF(`47 49 46`), WebP(`52 49 46 46 ... 57 45 42 50`)
- **트레이드오프**: HEIC, AVIF 등 미지원 포맷은 Content-Type 헤더로 폴백 → 헤더도 없으면 `image/jpeg` 기본값 사용. iOS 카메라가 HEIC를 생성하는 경우 `image_picker`가 JPEG로 변환하므로 현재 MVP에서는 문제없음.
