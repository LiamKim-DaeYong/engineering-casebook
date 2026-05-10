# Load Test

k6로 재고 예약 API의 전략별 HTTP 부하 테스트를 실행한다.

테스트 시작 시 같은 수량의 재고를 4개 생성하고, 각 재고를 서로 다른 전략 엔드포인트로 예약 요청한다.

- optimistic lock stock -> `/reservations/optimistic-lock`
- pessimistic lock stock -> `/reservations/pessimistic-lock`
- conditional update stock -> `/reservations/conditional-update`
- redis lua stock -> `/reservations/redis-lua`

Redis Lua 방식은 Redis 재고를 먼저 차감하고 Kafka에 `StockReserved` 이벤트를 발행한다. Kafka consumer가 RDB 재고를 비동기로 반영한다.

## Prerequisites

- Spring Boot app is running on `http://localhost:8080`
- PostgreSQL is running
- Redis is running
- Kafka is running
- k6 is installed

## Commands

```powershell
k6 run load-test/k6/reserve-stock.js
```

대표 시나리오:

```powershell
$env:INITIAL_STOCK="1000"
$env:ITERATIONS="1000"
$env:VUS="20"
$env:RESULT_NAME="kafka-stock1000-requests1000-vu20"
k6 run load-test/k6/reserve-stock.js

$env:INITIAL_STOCK="1000"
$env:ITERATIONS="2000"
$env:VUS="20"
$env:RESULT_NAME="kafka-stock1000-requests2000-vu20"
k6 run load-test/k6/reserve-stock.js
```

요청 수와 동시 사용자 수 변경:

```powershell
$env:VUS="100"
$env:ITERATIONS="500"
$env:INITIAL_STOCK="250"
k6 run load-test/k6/reserve-stock.js
```

## What To Compare

- strategy-level success count
- strategy-level failure rate
- strategy-level avg latency
- strategy-level p90 latency
- strategy-level p95 latency
- immediate `allocatedQuantity`
- immediate `availableQuantity`

`teardown` 단계에서 전략별 재고 상태를 JSON으로 출력한다. Redis Lua 방식은 Kafka consumer가 RDB를 비동기로 반영하므로, k6 종료 직후 수치와 몇 초 뒤 수치가 다를 수 있다.

`handleSummary` 단계에서 전략별 성능 지표를 파일로 저장한다.

```text
load-test/results/{RESULT_NAME}-summary.json
load-test/results/{RESULT_NAME}-summary.md
```

기본 `RESULT_NAME`은 `latest`다.
