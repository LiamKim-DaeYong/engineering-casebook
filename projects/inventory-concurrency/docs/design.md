# Design

## Strategies

### RDB optimistic lock

```text
request
-> select stock
-> update with @Version
-> response
```

고경합 상황에서는 version 충돌과 retry 초과가 발생할 수 있다.

### RDB pessimistic lock

```text
request
-> select for update
-> allocate stock
-> response
```

row lock으로 순차 처리하여 정합성을 확보한다.

### RDB conditional update

```text
request
-> update stocks
   set allocated_quantity = allocated_quantity + ?
   where id = ?
     and total_quantity - allocated_quantity >= ?
-> response
```

단순 재고 차감에서는 DB가 조건 검증과 변경을 한 번에 처리한다.

### Redis Lua with Kafka

```text
request
-> Redis Lua stock reservation
-> Kafka StockReserved event publish
-> response

Kafka consumer
-> RDB conditional update
```

Redis는 빠른 선점 계층이며, RDB 반영은 Kafka consumer가 비동기로 수행한다.

## Why Redis + Kafka

- 순간적으로 몰리는 요청을 DB보다 빠르게 처리할 수 있다.
- Lua script로 재고 확인과 차감을 하나의 Redis 작업으로 묶을 수 있다.
- 성공 요청만 Kafka로 넘겨 후속 RDB 반영을 비동기로 처리한다.
- 요청 경로에서 DB row update 경합을 제거할 수 있다.

## Risks

- Redis 차감은 성공했지만 Kafka 발행이 실패할 수 있다.
- Kafka consumer lag 때문에 RDB 반영은 eventual consistency가 된다.
- Redis 장애 시 예약 흐름을 어떻게 처리할지 결정해야 한다.
- Kafka event 중복 소비에 대한 idempotency 설계가 필요하다.
- 예약 만료가 필요하면 Redis 재고 복구와 DB 상태 변경을 함께 고려해야 한다.

## Out of Scope

이번 범위에서는 아래는 구현하지 않는다.

- Kafka consumer idempotency table
- dead letter topic
- reservation expiration
- reconciliation batch
