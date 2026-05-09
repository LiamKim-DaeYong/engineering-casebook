# Engineering Casebook

백엔드 개발 과정에서 학습한 내용과 문제 해결 사례를 정리하는 저장소입니다.

단순 개념 정리보다 실제 코드, 의사결정, 성능 지표, 트러블슈팅 과정을 중심으로 기록합니다.

## Structure

```text
engineering-casebook/
  notes/
    java/
    spring/
    database/
    architecture/
  cases/
    performance/
    debugging/
    refactoring/
    design-decisions/
  snippets/
```

## Writing Guide

- `notes/`: 개념, API, 프레임워크 동작 방식 정리
- `cases/`: 문제 상황, 접근 방식, 해결 과정, 결과를 남기는 사례 기록
- `snippets/`: 자주 쓰는 코드 조각, 설정, 명령어 모음

## Case Format

케이스 문서는 가능하면 아래 흐름으로 작성합니다.

```md
# 제목

## Problem
어떤 상황에서 문제가 발생했는지

## Approach
어떤 가설을 세웠고 어떻게 확인했는지

## Solution
어떤 코드/설계 변경을 했는지

## Result
쿼리 수, 응답 시간, 처리량 등 개선 수치

## Takeaway
다음에 비슷한 문제를 만나면 어떻게 판단할지
```

