package com.casebook.inventoryconcurrency.stock.application

class ConcurrentStockAllocationException(
    stockId: Long,
    cause: Throwable,
) : RuntimeException("stock allocation conflict: $stockId", cause)
