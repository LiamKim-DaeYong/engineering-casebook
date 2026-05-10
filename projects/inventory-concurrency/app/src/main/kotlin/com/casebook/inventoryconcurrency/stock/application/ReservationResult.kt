package com.casebook.inventoryconcurrency.stock.application

data class ReservationResult(
    val stockId: Long,
    val quantity: Long,
    val strategyType: ReservationStrategyType,
)
