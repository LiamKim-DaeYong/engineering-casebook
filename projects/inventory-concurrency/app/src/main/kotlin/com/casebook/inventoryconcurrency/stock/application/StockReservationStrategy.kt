package com.casebook.inventoryconcurrency.stock.application

interface StockReservationStrategy {
    val type: ReservationStrategyType

    fun reserve(command: ReserveStockCommand): ReservationResult
}
