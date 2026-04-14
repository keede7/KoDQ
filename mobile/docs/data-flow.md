# Data Flow

1. `HomeScreen` → `image_picker`로 이미지 선택
2. `ApiService.analyzeOutfit(File)` → `multipart/form-data`로 백엔드 전송
3. 백엔드 응답 `{ result, score, details }` → `AnalyzeResult`로 파싱
4. `ResultScreen` + `OutfitCard`로 결과 렌더링
