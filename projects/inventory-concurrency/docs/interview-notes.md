# Interview Notes

## One-liner

동시 주문 상황에서 재고 초과 예약을 막기 위해 RDB 락, 조건부 update, Redis Lua + Kafka 비동기 반영 방식을 비교했습니다.

## Explanation Points

- 재고 차감은 단순 CRUD가 아니라 동시성 상황에서 정합성이 깨질 수 있는 문제다.
- 단순 재고 차감에서는 RDB conditional update가 즉시 정합성과 성능의 균형이 좋았다.
- Redis Lua + Kafka 방식은 요청 경로에서 가장 빠른 선점 응답을 보였다.
- Redis 방식은 RDB 반영이 비동기이므로 consumer lag, 중복 처리, 보상/복구 설계가 필요하다.
- 낙관적 락은 단일 row 고경합 상황에서 retry 초과로 실패율이 높았다.
