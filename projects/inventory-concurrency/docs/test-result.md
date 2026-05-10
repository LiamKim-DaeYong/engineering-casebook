# Test Result

## Latest Benchmarks

DB 기반 3가지 방식과 Redis Lua + Kafka 방식을 k6로 비교했다.

조건:

- 단일 재고 row
- 요청 수량은 모두 1
- 재고 수는 전략별 1000개
- VU는 전략별 20
- 예약 API 응답에는 최신 재고 수량을 포함하지 않음
- Redis 방식은 요청 경로에서 Redis 선점과 Kafka 발행까지만 수행하고, RDB 반영은 consumer가 비동기로 수행

### Case 1. Stock 1000, Requests 1000

| Strategy | Success | Failure Rate | Avg(ms) | P95(ms) |
| --- | ---: | ---: | ---: | ---: |
| JPA_OPTIMISTIC_LOCK | 727 | 27.30% | 63.00 | 132.95 |
| JPA_PESSIMISTIC_LOCK | 1000 | 0.00% | 58.00 | 96.77 |
| CONDITIONAL_UPDATE | 1000 | 0.00% | 48.44 | 78.10 |
| REDIS_LUA | 1000 | 0.00% | 5.07 | 8.29 |

Redis 전략은 k6 종료 직후에는 Kafka consumer의 RDB 반영이 완료되지 않았다.

```text
immediately after k6:
allocatedQuantity = 116
availableQuantity = 884

after 5 seconds:
allocatedQuantity = 1000
availableQuantity = 0
```

### Case 2. Stock 1000, Requests 2000

| Strategy | Success | Failure Rate | Avg(ms) | P95(ms) |
| --- | ---: | ---: | ---: | ---: |
| JPA_OPTIMISTIC_LOCK | 1000 | 50.00% | 55.24 | 128.05 |
| JPA_PESSIMISTIC_LOCK | 1000 | 50.00% | 45.93 | 100.78 |
| CONDITIONAL_UPDATE | 1000 | 50.00% | 41.55 | 84.45 |
| REDIS_LUA | 1000 | 50.00% | 5.86 | 11.16 |

Redis 전략은 k6 종료 직후에는 Kafka consumer의 RDB 반영이 완료되지 않았다.

```text
immediately after k6:
allocatedQuantity = 238
availableQuantity = 762

after 5 seconds:
allocatedQuantity = 1000
availableQuantity = 0
```

## Takeaway

요청 경로 기준으로는 `REDIS_LUA`가 가장 낮은 평균 지연 시간과 p95를 보였다.

`REDIS_LUA`는 Redis에서 빠르게 선점하고 Kafka로 후속 처리를 넘기기 때문에, RDB 반영은 eventual consistency가 된다.

즉시 정합성이 필요한 RDB 방식 중에서는 `CONDITIONAL_UPDATE`가 가장 낮은 평균 지연 시간과 p95를 보였다.

`JPA_PESSIMISTIC_LOCK`은 안정적으로 재고를 모두 할당했지만, row lock 대기 비용 때문에 `CONDITIONAL_UPDATE`보다 느렸다.

`JPA_OPTIMISTIC_LOCK`은 단일 row에 요청이 집중되는 고경합 상황에서 version 충돌과 retry 초과로 실패율이 높았다.
