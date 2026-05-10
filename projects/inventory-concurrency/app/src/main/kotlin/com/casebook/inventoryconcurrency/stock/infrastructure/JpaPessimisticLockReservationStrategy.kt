package com.casebook.inventoryconcurrency.stock.infrastructure

import com.casebook.inventoryconcurrency.stock.application.ReservationResult
import com.casebook.inventoryconcurrency.stock.application.ReservationStrategyType
import com.casebook.inventoryconcurrency.stock.application.ReserveStockCommand
import com.casebook.inventoryconcurrency.stock.application.StockReservationStrategy
import com.casebook.inventoryconcurrency.stock.domain.StockRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class JpaPessimisticLockReservationStrategy(
    private val stockRepository: StockRepository,
) : StockReservationStrategy {

    override val type: ReservationStrategyType = ReservationStrategyType.JPA_PESSIMISTIC_LOCK

    @Transactional
    override fun reserve(command: ReserveStockCommand): ReservationResult {
        val stock = stockRepository.findByIdForUpdate(command.stockId)
            ?: throw NoSuchElementException("stock not found: ${command.stockId}")

        stock.allocate(command.quantity)

        return ReservationResult(
            stockId = command.stockId,
            quantity = command.quantity,
            strategyType = type,
        )
    }
}
