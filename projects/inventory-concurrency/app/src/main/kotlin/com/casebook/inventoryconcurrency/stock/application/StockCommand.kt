package com.casebook.inventoryconcurrency.stock.application

data class CreateStockCommand(
    val sku: String,
    val totalQuantity: Long,
)

data class ReserveStockCommand(
    val stockId: Long,
    val quantity: Long,
)
