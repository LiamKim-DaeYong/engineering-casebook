# Problem

동시에 여러 주문 요청이 들어오면 같은 재고를 여러 요청이 함께 차감하려고 할 수 있다.

이 프로젝트는 재고 수량보다 많은 요청이 동시에 들어오는 상황에서 초과 예약이 발생하지 않도록 하는 방식을 검증한다.

## Scenario

- 상품 재고는 1000개다.
- 총 2000개의 예약 요청이 들어온다.
- 여러 사용자가 같은 재고에 동시에 접근한다.
- 성공한 예약은 최대 1000건이어야 한다.
- 초과 요청 1000건은 실패해야 한다.
- 최종 재고는 음수가 되면 안 된다.

## Scope

비교 대상은 네 가지다.

- RDB optimistic lock
- RDB pessimistic lock
- RDB conditional update
- Redis Lua reservation with Kafka async confirmation
