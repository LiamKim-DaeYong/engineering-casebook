package com.casebook.inventoryconcurrency.stock.application

import com.casebook.inventoryconcurrency.stock.domain.Stock
import com.casebook.inventoryconcurrency.stock.domain.StockRepository
import com.casebook.inventoryconcurrency.stock.domain.toResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StockService(
    private val stockRepository: StockRepository,
    strategies: List<StockReservationStrategy>,
) {
    private val strategyMap = strategies.associateBy { it.type }

    @Transactional
    fun create(command: CreateStockCommand): StockResult {
        val stock = Stock(
            sku = command.sku,
            totalQuantity = command.totalQuantity,
        )

        return stockRepository.save(stock).toResult()
    }

    @Transactional(readOnly = true)
    fun get(stockId: Long): StockResult {
        val stock = stockRepository.findById(stockId)
            .orElseThrow { NoSuchElementException("stock not found: $stockId") }

        return stock.toResult()
    }

    fun reserve(command: ReserveStockCommand, strategyType: ReservationStrategyType): ReservationResult {
        val strategy = strategyMap[strategyType]
            ?: throw IllegalArgumentException("unsupported strategy: $strategyType")

        return strategy.reserve(command)
    }
}
