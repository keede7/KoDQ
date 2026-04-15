# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# 프로젝트: KoDQ

의상 사진 한 장을 올리면 AI가 상의·하의 조합의 코디 궁합을 분석해 판정·점수·피드백을 반환하는 플랫폼.

## 기술 스택

- Ktor 3.0.3 / Kotlin 2.0.21 — 백엔드 REST API
- Gradle Kotlin DSL — 빌드
- Flutter (Dart) / iOS 우선 — 모바일 앱
- Claude Vision API (`claude-sonnet-4-6`) — AI 코디 분석
- dotenv-kotlin 6.4.1 — 환경변수 관리 (`ignoreIfMissing = true`)

## 아키텍처 규칙

- CRITICAL: 모든 Claude API 호출은 `ClaudeService`에서만 수행. `routes/`에서 직접 호출 금지
- CRITICAL: 이미지 Base64 인코딩은 서버(`ClaudeService`)에서만 수행. 클라이언트 인코딩 후 전송 금지
- CRITICAL: Claude 응답 파싱 시 마크다운 코드블록(` ```json `) 제거 로직 반드시 포함
- CRITICAL: `backend/.env`는 절대 커밋 금지 (gitignore 처리됨)
- 이미지 mediaType은 magic bytes로 서버에서 감지 (`detectMediaType` 함수, `routes/StyleRoutes.kt`)
- 백엔드 라우트 로직은 `routes/`, 외부 API 연동은 `services/`에만 위치
- 모바일 백엔드 URL은 `lib/services/api_service.dart`의 `_baseUrl` 상수로만 관리

## 개발 프로세스

- CRITICAL: 새 기능 구현 시 테스트 먼저 작성 후 구현 (TDD)
- 커밋 메시지는 conventional commits (`feat:`, `fix:`, `docs:`, `refactor:`, `chore:`)
- 배포 시 환경변수는 OS 환경변수로 주입 (`.env` 파일 미사용)

## 명령어

### 백엔드 (`backend/` 기준)

```bash
./gradlew run                                       # 개발 서버 실행
./gradlew buildFatJar                               # 배포용 Fat JAR 빌드
./gradlew test                                      # 전체 테스트
./gradlew test --tests "com.kodq.ApplicationTest"   # 단일 테스트 클래스
docker build -t kodq-backend .
docker run -p 8080:8080 --env-file .env kodq-backend
```

### 모바일 (`mobile/` 기준)

```bash
flutter pub get                        # 의존성 설치
flutter run                            # iOS 시뮬레이터 실행
flutter run -d <device-id>             # 특정 디바이스 지정
flutter analyze                        # lint
flutter test                           # 전체 테스트
flutter test test/widget_test.dart     # 단일 테스트 파일
flutter build ios --release            # iOS 릴리즈 빌드
```
