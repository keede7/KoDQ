# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

패션 코디 궁합 평가 플랫폼. 사용자가 촬영하거나 선택한 의상 사진을 Claude Vision API로 분석해 상의·하의 조합의 어울림 여부, 점수(0~100), 상세 피드백을 반환한다.

- `backend/` — Ktor 3.0.3 (Kotlin) 백엔드, Gradle Kotlin DSL
- `mobile/` — Flutter 앱 (iOS 우선, 크로스플랫폼)

## 명령어

### 백엔드 (`backend/` 디렉토리 기준)

```bash
./gradlew run                                        # 개발 서버 실행
./gradlew buildFatJar                                # 배포용 Fat JAR 빌드
./gradlew test                                       # 전체 테스트
./gradlew test --tests "com.kodq.ApplicationTest"    # 단일 테스트 클래스

docker build -t kodq-backend .
docker run -p 8080:8080 --env-file .env kodq-backend
```

### 모바일 (`mobile/` 디렉토리 기준)

```bash
flutter pub get           # 의존성 설치
flutter run               # iOS 시뮬레이터 실행
flutter run -d <id>       # 특정 디바이스 지정
flutter analyze           # lint
flutter test              # 전체 테스트
flutter test test/widget_test.dart   # 단일 테스트 파일
flutter build ios --release          # iOS 릴리즈 빌드
```

## 아키텍처

### 요청 흐름

```
Flutter (image_picker) 
  → ApiService.analyzeOutfit()       # multipart POST /api/analyze
  → Ktor StyleRoutes                 # 이미지 바이트 추출
  → ClaudeService.analyzeOutfit()    # Base64 인코딩 후 Claude API 호출
  → AnalyzeResponse { result, score, details }
  → ResultScreen                     # 결과 렌더링
```

### 백엔드 구조

- **`Application.kt`** — 서버 진입점, 플러그인 등록
- **`plugins/`** — HTTP(CORS), Routing, Serialization 설정
- **`routes/StyleRoutes.kt`** — `POST /api/analyze` 핸들러, multipart 파싱
- **`services/ClaudeService.kt`** — Claude Vision API 호출 및 응답 파싱, `AnalyzeResponse` 반환

Claude API 응답은 `content[].text`에서 JSON을 추출한다. 마크다운 코드블록(` ```json `)이 포함될 수 있어 제거 후 파싱한다. 파싱 실패 시 예외 → HTTP 500.

### 모바일 구조

- **`services/api_service.dart`** — 백엔드 통신 (`_baseUrl` 상수로 서버 주소 관리)
- **`screens/home_screen.dart`** — 이미지 선택/촬영 UI
- **`screens/result_screen.dart`** — 분석 결과 표시
- **`widgets/outfit_card.dart`** — 공통 카드 위젯

### API 계약

```
POST /api/analyze
Content-Type: multipart/form-data
  image: <파일>

→ 200 { "result": "어울림"|"안어울림", "score": 0~100, "details": "..." }
→ 500 오류 시
```

## 환경변수

백엔드 `backend/.env` 파일 (gitignore 처리됨):
```
ANTHROPIC_API_KEY=sk-ant-...
PORT=8080   # 선택, 기본값 8080
```

`dotenv-kotlin`으로 로드 (`ignoreIfMissing = true`). Docker/Railway/Fly.io 배포 시 환경변수 직접 주입 가능.

## 배포 대상

Railway 또는 Fly.io (1인 개발). 모바일은 iOS App Store 우선, 추후 Android 확장 예정.
