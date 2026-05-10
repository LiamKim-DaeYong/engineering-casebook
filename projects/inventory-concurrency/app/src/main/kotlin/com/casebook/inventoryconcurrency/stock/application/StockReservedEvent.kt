package com.casebook.inventoryconcurrency.stock.application

data class StockReservedEvent(
    val stockId: Long,
    val quantity: Long,
    val strategyType: String,
)
