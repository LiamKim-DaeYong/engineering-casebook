package com.casebook.inventoryconcurrency.stock.infrastructure

import com.casebook.inventoryconcurrency.stock.application.StockReservedEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class KafkaStockReservedProducer(
    private val kafkaTemplate: KafkaTemplate<String, StockReservedEvent>,
    @Value("\${app.kafka.topics.stock-reserved}")
    private val topic: String,
) {

    fun publish(event: StockReservedEvent) {
        kafkaTemplate.send(topic, event.stockId.toString(), event)
            .get(1, TimeUnit.SECONDS)
    }
}
