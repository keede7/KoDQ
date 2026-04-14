# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

패션 코디 궁합 평가 모바일 앱 (Flutter, iOS 우선). 사진 속 상의/하의의 색상·재질 조합을 Claude Vision API로 분석해 궁합 점수와 피드백을 제공한다.

전체 프로젝트는 `/Users/keede/spring/koder/` 아래 두 개의 서브프로젝트로 구성된다:
- `mobile/` — 이 Flutter 앱
- `backend/` — Ktor 백엔드 (Kotlin)

## 주요 명령어

```bash
# 의존성 설치
flutter pub get

# iOS 시뮬레이터에서 실행
flutter run

# 특정 디바이스 지정
flutter run -d <device-id>

# 분석 (lint)
flutter analyze

# 테스트
flutter test

# 단일 테스트 파일
flutter test test/widget_test.dart

# iOS 릴리즈 빌드
flutter build ios --release
```

## 아키텍처

`docs/architecture.md` 참고

## 데이터 흐름

`docs/data-flow.md` 참고

## 백엔드 연결

`api_service.dart`의 `_baseUrl`을 환경에 맞게 변경:
- 개발: `http://localhost:8080`
- 배포 후: Railway/Fly.io 서버 URL로 교체

백엔드 API: `POST /api/analyze` — `image` 필드로 이미지 파일 전송, JSON 응답:
```json
{ "result": "어울림" | "안어울림", "score": 0~100, "details": "설명 텍스트" }
```

