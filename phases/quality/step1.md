# Step 1: prompt-tuning

## 읽어야 할 파일

먼저 아래 파일들을 읽고 프로젝트의 아키텍처와 설계 의도를 파악하라:

- `/Users/keede/spring/KoDQ/docs/ARCHITECTURE.md`
- `/Users/keede/spring/KoDQ/docs/ADR.md`
- `/Users/keede/spring/KoDQ/docs/PRD.md`
- `/Users/keede/spring/KoDQ/backend/src/main/kotlin/com/kodq/services/ClaudeService.kt`
- `/Users/keede/spring/KoDQ/phases/quality/index.json` (이전 step 완료 여부 확인)

이전 step에서 수정된 코드를 읽고 맥락을 파악한 뒤 작업하라.

## 작업

### 목표

`ClaudeService.kt`의 Claude API 프롬프트를 개선해 분석 품질을 높인다.

현재 프롬프트의 문제점:
1. 분석 기준(색상, 재질, 스타일)이 명시되어 있으나 각 기준별 판단 로직이 없다
2. score 0~100의 근거 기준이 없어 일관성이 떨어진다
3. details가 2~5문장으로만 명시되어 있어 구조가 없다
4. 의상이 없는 사진(풍경, 음식)에 대한 처리 지시가 없다

### 개선 방향

**`backend/src/main/kotlin/com/kodq/services/ClaudeService.kt`** 의 `prompt` 변수만 수정한다.

프롬프트에 포함할 내용:

1. **역할 설정**: 패션 스타일리스트로서 전문적 분석 요청
2. **분석 기준 구체화**:
   - 색상 조화: 보색, 유사색, 무채색 조합 등 명시적 판단
   - 재질/텍스처: 계절감, 포멀/캐주얼 매치 여부
   - 스타일 통일성: 전체적인 무드와 스타일 방향성
3. **score 기준 명시**:
   - 90~100: 완벽한 조화, 전문 스타일리스트 수준
   - 70~89: 잘 어울리며 일상 착용에 적합
   - 50~69: 무난하나 개선 여지 있음
   - 30~49: 어색한 조합, 수정 필요
   - 0~29: 심각하게 어울리지 않음
4. **의상 없는 사진 처리**: 상의/하의가 식별되지 않으면 `result: "분석불가"`, `score: 0`, `details: "의상을 식별할 수 없습니다..."` 반환하도록 명시
5. **보완 제안 강제화**: `score < 70`이면 details 마지막에 "개선 제안: ..." 형식으로 구체적 보완점 포함
6. **JSON 형식 엄격화**: 마크다운 코드블록 없이 순수 JSON만 반환하도록 재강조

프롬프트는 한국어로 작성한다. trimIndent()를 활용해 가독성 있게 유지한다.

### 주의

- `AnalyzeResponse` 데이터 클래스 구조(`result`, `score`, `details`)는 변경하지 마라. 모바일 클라이언트와의 API 계약이다.
- `result` 값은 `"어울림"`, `"안어울림"`, `"분석불가"` 세 가지만 허용한다. 모바일 `outfit_card.dart`가 이 값으로 분기하므로 다른 값을 추가하지 마라.
- `ClaudeService` 외 다른 파일은 수정하지 마라.

## Acceptance Criteria

```bash
cd /Users/keede/spring/KoDQ/backend
./gradlew test
```

컴파일 에러 없음 + 기존 테스트 모두 통과.

## 검증 절차

1. 위 AC 커맨드를 실행한다.
2. 체크리스트:
   - `AnalyzeResponse` 구조 변경 없음을 확인
   - `result` 허용값이 세 가지인지 확인 (프롬프트 텍스트 검토)
   - `ClaudeService.kt` 외 파일 수정 없음 확인 (`git diff` 확인)
3. 결과에 따라 `phases/quality/index.json`의 step 1을 업데이트한다:
   - 성공 → `"status": "completed"`, `"summary": "산출물 한 줄 요약"`
   - 수정 3회 시도 후에도 실패 → `"status": "error"`, `"error_message": "구체적 에러 내용"`
   - 사용자 개입 필요 → `"status": "blocked"`, `"blocked_reason": "구체적 사유"` 후 즉시 중단

## 금지사항

- `AnalyzeResponse` 필드를 추가/제거/변경하지 마라. 이유: 모바일 API 계약이 깨진다.
- `result` 값에 `"어울림"`, `"안어울림"`, `"분석불가"` 외 다른 문자열을 허용하는 프롬프트를 쓰지 마라. 이유: 모바일 `outfit_card.dart`의 분기 로직이 깨진다.
- HTTP 요청 로직, 에러 핸들링, 파싱 로직은 건드리지 마라. 프롬프트 문자열만 수정한다.
