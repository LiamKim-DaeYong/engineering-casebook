package com.casebook.inventoryconcurrency.stock.application

data class StockResult(
    val id: Long,
    val sku: String,
    val totalQuantity: Long,
    val allocatedQuantity: Long,
    val availableQuantity: Long,
)
