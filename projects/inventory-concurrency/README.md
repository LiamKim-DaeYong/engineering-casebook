# Inventory Concurrency

동시 주문 상황에서 재고 정합성을 지키는 방식을 실험하는 미니 프로젝트입니다.

RDB 기반 재고 차감 방식과 Redis 기반 빠른 선점 방식을 같은 시나리오에서 비교합니다.

## Goal

- 동시에 여러 요청이 들어와도 재고가 음수가 되지 않도록 한다.
- RDB 기반 방식은 요청 경로에서 즉시 재고를 반영한다.
- Redis 기반 방식은 요청 경로에서 빠르게 선점하고 Kafka consumer가 RDB에 비동기 반영한다.
- 테스트 결과를 문서화해서 면접에서 설명 가능한 사례로 만든다.

## Structure

```text
inventory-concurrency/
  app/      # Spring Boot, Kotlin, JPA application
  docker/   # PostgreSQL, Redis, Kafka local environment
  docs/     # problem, design, test result, interview notes
  load-test/ # k6 scripts and benchmark results
```

## Local Environment

```powershell
cd projects/inventory-concurrency
docker compose -f docker/docker-compose.yml up -d
cd app
.\\gradlew.bat bootRun
```

PowerShell 기준 명령이다. macOS/Linux에서는 `./gradlew bootRun`을 사용한다.

## Stack

- Kotlin
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Redis
- Kafka
- Docker Compose

## Documents

- [Problem](docs/problem.md)
- [Design](docs/design.md)
- [Test Result](docs/test-result.md)
- [Interview Notes](docs/interview-notes.md)
