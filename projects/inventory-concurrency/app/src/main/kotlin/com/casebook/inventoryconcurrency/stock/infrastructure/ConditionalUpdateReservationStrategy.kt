package com.casebook.inventoryconcurrency.stock.infrastructure

import com.casebook.inventoryconcurrency.stock.application.ReservationResult
import com.casebook.inventoryconcurrency.stock.application.ReservationStrategyType
import com.casebook.inventoryconcurrency.stock.application.ReserveStockCommand
import com.casebook.inventoryconcurrency.stock.application.StockReservationStrategy
import com.casebook.inventoryconcurrency.stock.domain.StockRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ConditionalUpdateReservationStrategy(
    private val stockRepository: StockRepository,
) : StockReservationStrategy {

    override val type: ReservationStrategyType = ReservationStrategyType.CONDITIONAL_UPDATE

    @Transactional
    override fun reserve(command: ReserveStockCommand): ReservationResult {
        val updatedRows = stockRepository.allocateIfAvailable(
            id = command.stockId,
            quantity = command.quantity,
        )

        check(updatedRows == 1) { "not enough stock" }

        return ReservationResult(
            stockId = command.stockId,
            quantity = command.quantity,
            strategyType = type,
        )
    }
}
