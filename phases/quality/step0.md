# Step 0: backend-tests

## 읽어야 할 파일

먼저 아래 파일들을 읽고 프로젝트의 아키텍처와 설계 의도를 파악하라:

- `/Users/keede/spring/KoDQ/docs/ARCHITECTURE.md`
- `/Users/keede/spring/KoDQ/docs/ADR.md`
- `/Users/keede/spring/KoDQ/backend/src/main/kotlin/com/kodq/routes/StyleRoutes.kt`
- `/Users/keede/spring/KoDQ/backend/src/main/kotlin/com/kodq/services/ClaudeService.kt`
- `/Users/keede/spring/KoDQ/backend/src/main/kotlin/com/kodq/Application.kt`
- `/Users/keede/spring/KoDQ/backend/src/main/kotlin/com/kodq/plugins/Routing.kt`
- `/Users/keede/spring/KoDQ/backend/build.gradle.kts`

코드를 꼼꼼히 읽고 설계 의도를 파악한 뒤 작업하라.

## 작업

### 목표

백엔드 테스트를 작성한다. CLAUDE.md에 TDD가 명시되어 있으나 현재 테스트가 전무하다.
`build.gradle.kts`에 `ktor-server-test-host`와 `kotlin-test-junit`이 이미 선언되어 있으므로 추가 의존성 없이 작성한다.

### 1. StyleRoutes 테스트 가능하도록 리팩토링

현재 `styleRoutes()`는 내부에서 `ClaudeService()`를 직접 생성한다. 테스트에서 mock을 주입할 수 없으므로 시그니처를 변경한다.

**`backend/src/main/kotlin/com/kodq/routes/StyleRoutes.kt`**

- `detectMediaType` 함수를 `private` → `internal`로 변경한다. (같은 파일 내 위치 유지)
- `styleRoutes()` 함수 시그니처를 `fun Route.styleRoutes(claudeService: ClaudeService = ClaudeService())`로 변경한다. 기본값이 있으므로 기존 호출부(`Routing.kt`)는 수정 불필요.

### 2. 테스트 파일 생성

**`backend/src/test/kotlin/com/kodq/DetectMediaTypeTest.kt`**

`detectMediaType` 함수에 대한 단위 테스트:
- JPEG magic bytes (`0xFF, 0xD8`) + 나머지 패딩 → `"image/jpeg"` 반환
- PNG magic bytes (`0x89, 0x50, 0x4E`) + 나머지 패딩 → `"image/png"` 반환
- GIF magic bytes (`0x47, 0x49, 0x46`) + 나머지 패딩 → `"image/gif"` 반환
- WebP magic bytes (`0x52, 0x49, 0x46, 0x46, 0x00×4, 0x57, 0x45, 0x42, 0x50`) → `"image/webp"` 반환
- 인식 불가 bytes → `null` 반환
- 12바이트 미만 배열 → `null` 반환

`internal` 함수는 같은 모듈의 테스트 소스셋에서 바로 접근 가능하다.

**`backend/src/test/kotlin/com/kodq/StyleRoutesTest.kt`**

`testApplication` 블록을 이용한 통합 테스트:

```kotlin
// FakeClaudeService: ClaudeService를 상속하거나 동일 시그니처의 suspend fun을 가진 테스트용 구현체
// ClaudeService가 open class가 아니라면 open으로 변경하거나 별도 인터페이스 추출을 고려하라
```

- **케이스 1 — 이미지 파트 누락 시 HTTP 400**:
  - multipart body 없이 `POST /api/analyze` 요청
  - 응답 상태코드 `400` 확인
  - 응답 body JSON에 `"error"` 키 포함 확인

- **케이스 2 — ClaudeService 예외 시 HTTP 500**:
  - FakeClaudeService가 `RuntimeException` 던지도록 구성
  - 유효한 JPEG 바이트(최소 12바이트 이상)를 multipart `"image"` 필드로 전송
  - 응답 상태코드 `500` 확인
  - 응답 body JSON에 `"error"` 키 포함 확인

- **케이스 3 — 정상 응답 시 HTTP 200**:
  - FakeClaudeService가 `AnalyzeResponse("어울림", 85, "테스트 피드백")` 반환하도록 구성
  - 유효한 JPEG 바이트를 multipart `"image"` 필드로 전송
  - 응답 상태코드 `200` 확인
  - 응답 body JSON에 `"result"`, `"score"`, `"details"` 키 포함 확인

ClaudeService를 mock 주입 가능하게 만들기 위해 필요하다면 `open class`로 변경하거나 interface를 추출해도 된다. 단, 기존 `ClaudeService` 동작은 변경하지 마라.

## Acceptance Criteria

```bash
cd /Users/keede/spring/KoDQ/backend
./gradlew test
```

컴파일 에러 없음 + 모든 테스트 통과.

## 검증 절차

1. 위 AC 커맨드를 실행한다.
2. 아키텍처 체크리스트:
   - ARCHITECTURE.md의 `routes/` vs `services/` 분리 구조를 유지하는가?
   - `ClaudeService` 실제 동작(API 호출)을 변경하지 않았는가?
   - CLAUDE.md의 CRITICAL 규칙을 위반하지 않았는가?
3. 결과에 따라 `phases/quality/index.json`의 step 0을 업데이트한다:
   - 성공 → `"status": "completed"`, `"summary": "산출물 한 줄 요약"`
   - 수정 3회 시도 후에도 실패 → `"status": "error"`, `"error_message": "구체적 에러 내용"`
   - 사용자 개입 필요 → `"status": "blocked"`, `"blocked_reason": "구체적 사유"` 후 즉시 중단

## 금지사항

- `ClaudeService`에서 실제 Claude API를 호출하는 로직을 변경하지 마라. 테스트에서만 mock을 쓴다.
- `@IgnoreIfMissing` dotenv 설정을 건드리지 마라. 테스트 환경에서 `.env`가 없어도 동작해야 한다.
- 기존 production 코드의 동작을 바꾸는 리팩토링은 테스트 가능성을 위한 최소한만 수행한다 (시그니처 변경, open 추가 등).
