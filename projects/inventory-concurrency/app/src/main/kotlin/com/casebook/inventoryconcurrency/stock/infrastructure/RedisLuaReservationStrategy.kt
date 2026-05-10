package com.casebook.inventoryconcurrency.stock.infrastructure

import com.casebook.inventoryconcurrency.stock.application.ReservationResult
import com.casebook.inventoryconcurrency.stock.application.ReservationStrategyType
import com.casebook.inventoryconcurrency.stock.application.ReserveStockCommand
import com.casebook.inventoryconcurrency.stock.application.StockReservationStrategy
import com.casebook.inventoryconcurrency.stock.application.StockReservedEvent
import com.casebook.inventoryconcurrency.stock.domain.StockRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component

@Component
class RedisLuaReservationStrategy(
    private val redisTemplate: StringRedisTemplate,
    private val redisStockStoreAdapter: RedisStockStoreAdapter,
    private val stockRepository: StockRepository,
    private val kafkaStockReservedProducer: KafkaStockReservedProducer,
) : StockReservationStrategy {

    override val type: ReservationStrategyType = ReservationStrategyType.REDIS_LUA

    override fun reserve(command: ReserveStockCommand): ReservationResult {
        reserveInRedis(command)

        try {
            kafkaStockReservedProducer.publish(
                StockReservedEvent(
                    stockId = command.stockId,
                    quantity = command.quantity,
                    strategyType = type.name,
                ),
            )

            return ReservationResult(
                stockId = command.stockId,
                quantity = command.quantity,
                strategyType = type,
            )
        } catch (exception: RuntimeException) {
            restoreRedisStock(command)
            throw exception
        }
    }

    private fun reserveInRedis(command: ReserveStockCommand) {
        val result = reserveInRedisIfInitialized(command)

        if (result == MISSING_KEY) {
            initializeRedisStock(command.stockId)
            check(reserveInRedisIfInitialized(command) == RESERVED) { "not enough redis stock" }
            return
        }

        check(result == RESERVED) { "not enough redis stock" }
    }

    private fun reserveInRedisIfInitialized(command: ReserveStockCommand): Long {
        return redisTemplate.execute(
            RESERVE_SCRIPT,
            listOf(redisStockStoreAdapter.key(command.stockId)),
            command.quantity.toString(),
        ) ?: error("redis script result is null")
    }

    private fun restoreRedisStock(command: ReserveStockCommand) {
        redisStockStoreAdapter.restore(command.stockId, command.quantity)
    }

    private fun initializeRedisStock(stockId: Long) {
        val stock = stockRepository.findById(stockId)
            .orElseThrow { NoSuchElementException("stock not found: $stockId") }

        redisStockStoreAdapter.initializeIfAbsent(
            stockId = stock.id,
            totalQuantity = stock.totalQuantity,
        )
    }

    companion object {
        private const val MISSING_KEY = -1L
        private const val RESERVED = 1L

        private val RESERVE_SCRIPT = DefaultRedisScript(
            """
            if redis.call('EXISTS', KEYS[1]) == 0 then
              return -1
            end

            local current = tonumber(redis.call('GET', KEYS[1]))
            local quantity = tonumber(ARGV[1])

            if current < quantity then
              return 0
            end

            redis.call('DECRBY', KEYS[1], quantity)
            return 1
            """.trimIndent(),
            Long::class.java,
        )
    }
}
