package com.casebook.inventoryconcurrency.stock.application

enum class ReservationStrategyType {
    CONDITIONAL_UPDATE,
    JPA_OPTIMISTIC_LOCK,
    JPA_PESSIMISTIC_LOCK,
    REDIS_LUA,
}
