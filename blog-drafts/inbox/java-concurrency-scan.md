# 블로그 후보 스캔

## 스캔 범위

- 대상 디렉토리: `notes/java`
- 기준일: 2026-06-14
- 읽은 범위:
  - 파일 메타데이터: 전체
  - 제목/섹션 제목: 전체
  - 부분 본문: `java-producer-consumer-wait-notify.md`, `java-locksupport-reentrantlock.md`
  - 전체 본문: 없음
- 기준:
  - 최근 생성/수정 문서
  - 파일명 기준 주제 묶음
  - 문서 제목과 섹션 제목 기준의 1차 맥락 확인
  - 기존 블로그와의 중복 가능성

## 1차 후보

| 후보 | source notes | 판단 | 이유 |
|---|---|---|---|
| 생산자-소비자 문제와 `wait`/`notify` | `notes/java/java-producer-consumer-wait-notify.md` | series-candidate | 최근 작성 문서이고, `synchronized`의 한계에서 `wait/notify`로 넘어가는 흐름이 명확하다. 단독 글로도 가능하지만 앞뒤 문서와 연결성이 강하다. |
| `LockSupport`와 `ReentrantLock` | `notes/java/java-locksupport-reentrantlock.md` | hold | 최근 문서이고 후속 주제로 자연스럽지만, 생산자-소비자 문제에서 `Condition`으로 넘어가는 연결 문서가 아직 별도로 정리되지 않았다. |
| `synchronized` | `notes/java/java-synchronized.md` | hold | 선행 개념으로 중요하지만 이미 notes 안에서 기초 정리 역할이 강하다. 독립 발행보다 후속 글의 배경 링크나 시리즈 1편 후보로 보는 편이 자연스럽다. |

## 주제 묶음

| 묶음 | 포함 문서 | 판단 |
|---|---|---|
| Java 동시성 기초 | `java-concurrency-basics.md`, `java-thread-info-lifecycle.md`, `java-thread-join.md`, `java-thread-interrupt-yield.md` | 블로그 단편보다 기반 지식 묶음에 가깝다. 바로 발행하기보다 이후 글의 선행 맥락으로 사용한다. |
| Java 공유 상태와 기본 동기화 | `java-volatile.md`, `java-synchronized.md` | `volatile`과 `synchronized` 비교 글 또는 시리즈 선행편 후보가 될 수 있다. |
| Java 조건 대기와 락 확장 | `java-producer-consumer-wait-notify.md`, `java-locksupport-reentrantlock.md` | 현재 가장 블로그 후보에 가깝다. `wait/notify`의 한계에서 `Condition`으로 넘어가는 흐름을 만들 수 있다. |

## 보류 항목

| 문서 | 보류 이유 | 다음 확인 |
|---|---|---|
| `notes/java/java-concurrency-basics.md` | 범위가 넓고 기초 개념이 많아 블로그 글 한 편으로는 초점이 흐려질 수 있다. | 특정 후속 글의 도입 배경으로 재사용할지 확인한다. |
| `notes/java/java-thread-info-lifecycle.md` | 스레드 상태 정리로는 좋지만 독립 블로그 글로는 문제의식이 약할 수 있다. | `wait/notify` 글에서 `WAITING`, `BLOCKED` 상태 설명이 필요할 때 참조한다. |
| `notes/java/java-thread-join.md` | 독립 개념 정리 성격이 강하다. | 스레드 대기 메서드 비교 글을 만들 때 묶을 수 있는지 확인한다. |
| `notes/java/java-thread-interrupt-yield.md` | 현재 생산자-소비자 축과 직접 연결성은 낮다. | 스레드 제어 또는 취소 처리 주제로 별도 묶음이 생기면 다시 본다. |

## 시리즈/단편 판단

현재 상태에서는 `java-producer-consumer-wait-notify.md`를 바로 단편으로 발행하기보다,
`Java 동시성에서 조건 대기를 이해하는 흐름`의 일부로 보는 편이 더 자연스럽다.

가능한 제목 방향:

- 단편형: `생산자-소비자 문제로 이해하는 wait/notify`
- 시리즈형: `[Java 동시성] 생산자-소비자 문제와 wait/notify`

시리즈로 간다면 예상 흐름은 아래가 자연스럽다.

1. `synchronized`는 무엇을 해결하고 무엇을 해결하지 못하는가
2. 생산자-소비자 문제와 `wait`/`notify`
3. `notify`의 한계와 `Condition`이 필요한 이유

## 중복 확인

- 현재 이 시뮬레이션에서는 기존 블로그 원본 전체를 다시 조회하지 않았다.
- 기존 블로그의 최근 확인 기준으로는 Java 동시성 글이 중심 목록에 보이지 않았지만, 실제 초안 생성 전에는 블로그 저장소의 `src/data/blog`를 다시 확인해야 한다.

## 다음 액션

- 생성할 초안: 아직 생성하지 않음
- 추가로 읽을 notes:
  - `notes/java/java-synchronized.md`
  - `notes/java/java-thread-info-lifecycle.md`
  - `notes/java/java-locksupport-reentrantlock.md`
- 확인할 기존 블로그 글:
  - Java 동시성 관련 기존 발행 글 여부
  - `synchronized`, `wait`, `notify`, `reentrantlock` 태그 중복 여부

## 시뮬레이션 결과

파이프라인은 후보를 좁히는 데는 동작했다.
다만 실제 사용성을 높이려면 후보 스캔 템플릿에 아래 항목이 추가되는 편이 좋다.

- 어느 수준까지 읽었는지 남기는 `read depth`
- 기존 블로그 중복 확인 여부
- 최종 결정 상태: `draft-now`, `hold`, `needs-more-notes`
