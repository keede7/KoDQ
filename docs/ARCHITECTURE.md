# 아키텍처

## 구성

```
KoDQ/
├── backend/                         # Ktor (Kotlin) REST API 서버
│   └── src/main/kotlin/com/kodq/
│       ├── Application.kt           # 서버 진입점, 플러그인 등록
│       ├── plugins/                 # HTTP(CORS), Routing, Serialization 설정
│       ├── routes/
│       │   └── StyleRoutes.kt       # POST /api/analyze 핸들러 + detectMediaType
│       └── services/
│           └── ClaudeService.kt     # Claude API 호출, 응답 파싱, AnalyzeResponse 반환
└── mobile/                          # Flutter 앱 (iOS 우선)
    └── lib/
        ├── screens/
        │   ├── home_screen.dart     # 이미지 선택/촬영, 에러 SnackBar 처리
        │   └── result_screen.dart   # 분석 결과 표시
        ├── services/
        │   └── api_service.dart     # 백엔드 HTTP 통신, _baseUrl 관리
        └── widgets/
            └── outfit_card.dart     # 공통 카드 위젯
```

## 정상 요청 흐름

```
사용자 (사진 선택/촬영)
  │
  ▼
HomeScreen._pickAndAnalyze()
  │  XFile (image_picker)
  ▼
ApiService.analyzeOutfit()
  │  multipart POST /api/analyze  (field: "image")
  ▼
StyleRoutes: POST /api/analyze
  │  multipart 파싱 → ByteArray 추출
  │  detectMediaType(bytes) — magic bytes로 포맷 감지
  ▼
ClaudeService.analyzeOutfit(imageBytes, mediaType)
  │  Base64 인코딩
  │  POST https://api.anthropic.com/v1/messages
  │  (model: claude-sonnet-4-6, max_tokens: 1024)
  ▼
Claude API 응답
  │  content[].text 추출
  │  마크다운 코드블록(```json ... ```) 제거
  │  JSON 파싱 → AnalyzeResponse
  ▼
HTTP 200  { "result", "score", "details" }
  │
  ▼
ResultScreen  (판정 / 점수 / 상세 피드백 표시)
```

## 에러 처리 흐름

```
[케이스 1] image 파트 누락
StyleRoutes: imageBytes == null
  → HTTP 400 Bad Request
  → {"error": "이미지를 업로드해주세요."}

[케이스 2] Claude API HTTP 오류 (4xx / 5xx)
ClaudeService: response.status가 isSuccess() == false
  → println("Claude API error ${status}: ${body}")
  → throw RuntimeException("Claude API error: ${status} - ${body}")
  → StyleRoutes catch → HTTP 500
  → {"error": "Claude API error: ..."}

[케이스 3] 응답 JSON 파싱 실패
ClaudeService: json.parseToJsonElement(text) 예외
  → println("JSON parse error: ${text}")
  → throw RuntimeException("응답 파싱 실패: ${e.message}")
  → StyleRoutes catch → HTTP 500
  → {"error": "응답 파싱 실패: ..."}

[케이스 4] 모바일 네트워크 오류 / 서버 응답 없음
ApiService: HTTP 요청 예외
  → throw Exception("서버 오류: ${statusCode}") 또는 연결 예외
  → HomeScreen catch(e)
  → ScaffoldMessenger.showSnackBar("분석 중 오류가 발생했습니다: $e")
  → _isLoading = false (로딩 해제)
```

## API 계약

```
POST /api/analyze
Content-Type: multipart/form-data
  image: <파일 바이트>  (field name 반드시 "image")

성공
  HTTP 200
  { "result": "어울림" | "안어울림", "score": 0~100, "details": "..." }

실패
  HTTP 400  { "error": "이미지를 업로드해주세요." }
  HTTP 500  { "error": "<오류 메시지>" }
```

## 이미지 포맷 감지 (detectMediaType)

`StyleRoutes.kt`의 `detectMediaType(bytes: ByteArray)` 함수는 파일 헤더(magic bytes)로 포맷을 결정한다.
Content-Type 헤더는 클라이언트가 잘못 설정할 수 있어 신뢰하지 않음.

| 포맷 | 시그니처 | 바이트 |
|------|---------|--------|
| JPEG | `FF D8` | bytes[0..1] |
| PNG  | `89 50 4E` | bytes[0..2] |
| GIF  | `47 49 46` | bytes[0..2] |
| WebP | `52 49 46 46 ... 57 45 42 50` | bytes[0..3] + bytes[8..11] |

감지 실패 시 Content-Type 헤더 폴백, 헤더도 없으면 `image/jpeg` 기본값 사용.

## Claude API 응답 파싱 주의사항

Claude는 JSON 응답을 마크다운 코드블록으로 감쌀 수 있음. 반드시 제거 후 파싱:

```kotlin
val text = rawText.trim()
    .removePrefix("```json").removePrefix("```")
    .removeSuffix("```")
    .trim()
```

파싱 실패 시 `RuntimeException` → HTTP 500.

## 환경변수

| 변수명 | 필수 | 설명 |
|--------|------|------|
| `ANTHROPIC_API_KEY` | 필수 | Anthropic API 인증 키 |
| `PORT` | 선택 | 서버 포트 (기본값: 8080) |

`backend/.env`로 로컬 관리, 배포 환경(Railway/Fly.io)에서는 OS 환경변수로 주입.
