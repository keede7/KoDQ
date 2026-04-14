# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

KoDQ 앱의 Ktor 백엔드 (Kotlin). Flutter 앱에서 전달받은 의상 이미지를 Claude Vision API로 분석해 코디 궁합 결과를 반환한다.

## 주요 명령어

```bash
# 개발 서버 실행
./gradlew run

# Fat JAR 빌드 (배포용)
./gradlew buildFatJar

# 테스트
./gradlew test

# 단일 테스트 클래스
./gradlew test --tests "com.kodq.ApplicationTest"

# Docker 빌드
docker build -t kodq-backend .
docker run -p 8080:8080 --env-file .env kodq-backend
```

## 아키텍처

`docs/architecture.md` 참고

## 요청 흐름

`docs/request-flow.md` 참고

## 환경변수

`.env` 파일을 직접 생성해 아래 값을 설정:
- `ANTHROPIC_API_KEY` — Anthropic API 키 (필수)
- `PORT` — 서버 포트 (기본값: 8080)

`dotenv-kotlin` 라이브러리로 로드하며 `ignoreIfMissing = true`이므로 환경변수 직접 주입도 가능 (Docker/Railway/Fly.io 배포 시).

## Claude API 응답 파싱

`ClaudeService`는 응답 JSON에서 `content[].text`를 추출한 뒤, 마크다운 코드블록을 제거하고 JSON으로 파싱해 `AnalyzeResponse`를 구성한다. 파싱 실패 또는 API 오류 시 예외를 던지며, `StyleRoutes`에서 HTTP 500으로 클라이언트에 전달한다.
