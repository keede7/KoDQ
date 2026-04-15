# 아키텍처

## 구성

```
KoDQ/
├── backend/          # Ktor (Kotlin) REST API 서버
│   └── src/main/kotlin/com/kodq/
│       ├── plugins/  # HTTP(CORS), Routing, Serialization 설정
│       ├── routes/   # API 엔드포인트 핸들러
│       └── services/ # Claude Vision API 호출 및 응답 파싱
└── mobile/           # Flutter 앱 (iOS 우선)
    └── lib/
        ├── screens/  # 화면 (홈, 결과)
        ├── services/ # 백엔드 HTTP 통신
        └── widgets/  # 공통 UI 컴포넌트
```

## 데이터 흐름

```
사용자 (사진 선택/촬영)
  │
  ▼
HomeScreen (image_picker)
  │  XFile
  ▼
ApiService.analyzeOutfit()
  │  multipart POST /api/analyze
  ▼
Ktor StyleRoutes
  │  ByteArray + mediaType 추출
  ▼
ClaudeService.analyzeOutfit()
  │  Base64 인코딩 → Claude Vision API 호출
  │  (model: claude-sonnet-4-6, max_tokens: 1024)
  ▼
Claude API 응답 파싱
  │  content[].text → 마크다운 코드블록 제거 → JSON 파싱
  ▼
AnalyzeResponse { result, score, details }
  │  JSON HTTP 200
  ▼
ResultScreen
  │  판정 / 점수 / 상세 피드백 표시
```

## API 계약

```
POST /api/analyze
Content-Type: multipart/form-data
  image: <파일 바이트>

200 OK
{
  "result": "어울림" | "안어울림",
  "score": 0~100,
  "details": "색상·재질·스타일 조합에 대한 설명 (2~5문장)"
}

500 Internal Server Error  — Claude API 오류 또는 응답 파싱 실패
```

## 주요 설계 결정

- **이미지 전달 방식**: 모바일에서 Base64로 인코딩 후 JSON body에 포함하는 방식 대신, multipart로 raw 바이트를 전송하고 서버에서 Base64 인코딩. 모바일 메모리 부담 감소.
- **Claude 응답 파싱**: API가 마크다운 코드블록으로 JSON을 감싸는 경우가 있어 ` ```json ` prefix/suffix 제거 후 파싱.
- **환경변수**: `dotenv-kotlin`으로 `.env` 파일 로드 (`ignoreIfMissing = true`). 배포 환경에서는 OS 환경변수로 대체 가능.
