# Request Flow

1. `POST /api/analyze` — `multipart/form-data`로 이미지 수신 (`StyleRoutes.kt`)
2. magic bytes로 실제 미디어타입 자동 감지 (`detectMediaType`)
3. `ClaudeService.analyzeOutfit(ByteArray, mediaType)` 호출
4. 이미지를 Base64 인코딩 후 `claude-sonnet-4-6` 모델에 Vision 요청
5. Claude 응답에서 마크다운 코드블록 제거 후 JSON 파싱
6. `AnalyzeResponse(result, score, details)` 반환
