# Backend Architecture

```
src/main/kotlin/com/koder/
├── Application.kt             # 진입점: module()에서 플러그인 3개 마운트
├── plugins/
│   ├── Routing.kt             # styleRoutes() 등록
│   ├── Serialization.kt       # kotlinx.json ContentNegotiation
│   └── HTTP.kt                # CORS (anyHost, POST/GET/OPTIONS)
├── routes/
│   └── StyleRoutes.kt         # POST /api/analyze — multipart 수신, 미디어타입 감지, ClaudeService 호출
└── services/
    └── ClaudeService.kt       # Claude Vision API 호출, AnalyzeResponse 반환
```
