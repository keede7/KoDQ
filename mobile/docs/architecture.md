# Mobile Architecture

```
lib/
├── main.dart                  # 앱 진입점, MaterialApp 설정
├── screens/
│   ├── home_screen.dart       # 사진 촬영/갤러리 선택 → 분석 요청
│   └── result_screen.dart     # 분석 결과 표시 (이미지 + OutfitCard)
├── services/
│   └── api_service.dart       # 백엔드 POST /api/analyze 호출, AnalyzeResult 모델 정의
└── widgets/
    └── outfit_card.dart       # 점수 게이지 + 결과 텍스트 카드
```
