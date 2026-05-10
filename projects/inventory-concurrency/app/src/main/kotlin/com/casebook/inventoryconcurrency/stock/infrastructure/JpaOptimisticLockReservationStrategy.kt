package com.casebook.inventoryconcurrency.stock.infrastructure

import com.casebook.inventoryconcurrency.stock.application.ConcurrentStockAllocationException
import com.casebook.inventoryconcurrency.stock.application.ReservationResult
import com.casebook.inventoryconcurrency.stock.application.ReservationStrategyType
import com.casebook.inventoryconcurrency.stock.application.ReserveStockCommand
import com.casebook.inventoryconcurrency.stock.application.StockReservationStrategy
import com.casebook.inventoryconcurrency.stock.domain.StockRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class JpaOptimisticLockReservationStrategy(
    private val stockRepository: StockRepository,
    private val transactionTemplate: TransactionTemplate,
) : StockReservationStrategy {

    override val type: ReservationStrategyType = ReservationStrategyType.JPA_OPTIMISTIC_LOCK

    override fun reserve(command: ReserveStockCommand): ReservationResult {
        var lastConflict: OptimisticLockingFailureException? = null

        repeat(MAX_RETRY_COUNT) {
            try {
                return transactionTemplate.execute {
                    val stock = stockRepository.findById(command.stockId)
                        .orElseThrow { NoSuchElementException("stock not found: ${command.stockId}") }

                    stock.allocate(command.quantity)
                    stockRepository.flush()

                    ReservationResult(
                        stockId = command.stockId,
                        quantity = command.quantity,
                        strategyType = type,
                    )
                } ?: error("transaction result is null")
            } catch (exception: OptimisticLockingFailureException) {
                lastConflict = exception
            }
        }

        throw ConcurrentStockAllocationException(
            stockId = command.stockId,
            cause = lastConflict ?: IllegalStateException("optimistic lock retry exceeded"),
        )
    }

    companion object {
        private const val MAX_RETRY_COUNT = 3
    }
}
