package com.casebook.inventoryconcurrency.stock.infrastructure

import com.casebook.inventoryconcurrency.stock.application.StockReservedEvent
import com.casebook.inventoryconcurrency.stock.domain.StockRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class KafkaStockReservedConsumer(
    private val stockRepository: StockRepository,
) {

    @Transactional
    @KafkaListener(
        topics = ["\${app.kafka.topics.stock-reserved}"],
        groupId = "\${spring.kafka.consumer.group-id}",
    )
    fun handle(event: StockReservedEvent) {
        val updatedRows = stockRepository.allocateIfAvailable(
            id = event.stockId,
            quantity = event.quantity,
        )

        check(updatedRows == 1) { "failed to apply stock reserved event: $event" }
    }
}
