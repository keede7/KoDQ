# Step 2: mobile-ux

## 읽어야 할 파일

먼저 아래 파일들을 읽고 프로젝트의 아키텍처와 설계 의도를 파악하라:

- `/Users/keede/spring/KoDQ/docs/PRD.md` (디자인 방향: iOS 우선, Cupertino, 미니멀)
- `/Users/keede/spring/KoDQ/docs/ARCHITECTURE.md`
- `/Users/keede/spring/KoDQ/mobile/lib/main.dart`
- `/Users/keede/spring/KoDQ/mobile/lib/screens/home_screen.dart`
- `/Users/keede/spring/KoDQ/mobile/lib/screens/result_screen.dart`
- `/Users/keede/spring/KoDQ/mobile/lib/widgets/outfit_card.dart`
- `/Users/keede/spring/KoDQ/mobile/lib/services/api_service.dart`
- `/Users/keede/spring/KoDQ/mobile/pubspec.yaml`
- `/Users/keede/spring/KoDQ/phases/quality/index.json` (이전 step 완료 여부 확인)

이전 step에서 만들어진 코드를 읽고 맥락을 파악한 뒤 작업하라.

## 작업

### 목표

PRD 디자인 방향("iOS 우선, Cupertino 스타일 기반, 미니멀 UI")에 맞게 Flutter 앱의 UI/UX를 개선한다.
현재 앱은 Material Design 기반이며 iOS 감성이 부족하다.

### 1. `mobile/lib/main.dart` 수정

- `MaterialApp` → `CupertinoApp`으로 변환한다.
- `CupertinoApp`의 `theme`에 `CupertinoThemeData`를 설정한다 (primaryColor: `CupertinoColors.activeBlue`).
- `debugShowCheckedModeBanner: false` 추가.

### 2. `mobile/lib/screens/home_screen.dart` 수정

현재 `StatefulWidget` + Material 위젯 구조를 유지하면서 iOS 스타일로 교체한다.

- `Scaffold` → `CupertinoPageScaffold`
- `AppBar` → `CupertinoNavigationBar` (앱 이름 표시)
- 이미지 선택 버튼 2개(`CupertinoButton.filled`):
  - "카메라로 촬영" — `CupertinoIcons.camera`
  - "갤러리에서 선택" — `CupertinoIcons.photo`
- 로딩 상태: `CircularProgressIndicator` → `CupertinoActivityIndicator`
- 에러 표시: `SnackBar` → `showCupertinoDialog`로 변경 (확인 버튼 포함)
- 선택한 이미지 미리보기를 버튼 위에 표시한다 (최대 높이 250px, 둥근 모서리 12px)
- 이미지가 없을 때는 점선 테두리 + 아이콘 + 안내 문구 영역을 표시한다

### 3. `mobile/lib/screens/result_screen.dart` 수정

- `Scaffold` → `CupertinoPageScaffold`
- `AppBar` → `CupertinoNavigationBar` (제목: "코디 분석 결과")
- "다시 분석하기" → `CupertinoButton` (파괴적 액션 스타일: red 색상)
- 이미지와 결과 카드 사이 여백 및 패딩 개선

### 4. `mobile/lib/widgets/outfit_card.dart` 수정

- `Card` → 흰 배경 + `BoxDecoration`(둥근 모서리 16px, 그림자 최소화) 컨테이너로 교체
- 판정 텍스트 폰트 크기: 24sp, 굵게
- 점수 표시: 원형 프로그레스 (`CircularProgressIndicator` 대신 직접 `CustomPaint`로 그리거나, `ClipRect` + `LinearProgressIndicator` 유지)
  - 단, 색상을 iOS 스타일로: 점수 ≥70이면 `CupertinoColors.activeGreen`, 점수 < 70이면 `CupertinoColors.destructiveRed`
- 판정 아이콘 크기: 32px
- "분석불가" 판정 케이스 추가: 회색 아이콘(`CupertinoIcons.exclamationmark_circle`) + 회색 텍스트

### 5. `mobile/pubspec.yaml` 확인

`cupertino_icons: ^1.0.8`이 이미 선언되어 있는지 확인한다. 없으면 추가한다.
새 패키지를 추가하지 마라. 기존 패키지만 사용한다.

## Acceptance Criteria

```bash
cd /Users/keede/spring/KoDQ/mobile
flutter pub get
flutter analyze
flutter test
```

분석 경고 없음 (info 수준은 허용) + 테스트 통과.

## 검증 절차

1. 위 AC 커맨드를 순서대로 실행한다.
2. 체크리스트:
   - `api_service.dart`의 `_baseUrl` 변경 없음 확인
   - `AnalyzeResult` 모델 변경 없음 확인
   - `CupertinoApp`이 올바르게 설정되었는지 `main.dart` 검토
   - "분석불가" 판정 케이스가 `outfit_card.dart`에서 처리되는지 확인
3. 결과에 따라 `phases/quality/index.json`의 step 2를 업데이트한다:
   - 성공 → `"status": "completed"`, `"summary": "산출물 한 줄 요약"`
   - 수정 3회 시도 후에도 실패 → `"status": "error"`, `"error_message": "구체적 에러 내용"`
   - 사용자 개입 필요 → `"status": "blocked"`, `"blocked_reason": "구체적 사유"` 후 즉시 중단

## 금지사항

- `api_service.dart`의 `_baseUrl`을 변경하지 마라. 이유: 배포 설정은 별도 phase에서 다룬다.
- `AnalyzeResult` 데이터 모델을 수정하지 마라. 이유: 백엔드 API 계약이다.
- 새 패키지(`pubspec.yaml`)를 추가하지 마라. 이유: 의존성 최소화 원칙 + 빌드 환경 일관성.
- `home_screen.dart`의 이미지 분석 로직(`_pickAndAnalyze`)을 변경하지 마라. UI 레이어만 수정한다.
