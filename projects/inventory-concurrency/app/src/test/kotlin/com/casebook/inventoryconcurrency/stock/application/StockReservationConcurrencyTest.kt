package com.casebook.inventoryconcurrency.stock.application

import com.casebook.inventoryconcurrency.stock.domain.StockRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

@SpringBootTest
class StockReservationConcurrencyTest @Autowired constructor(
    private val stockService: StockService,
    private val stockRepository: StockRepository,
) {

    @AfterEach
    fun tearDown() {
        stockRepository.deleteAll()
    }

    @Test
    fun `optimistic lock and pessimistic lock reserve stock without over allocation`() {
        val conditionalUpdateResult = runConcurrentReservation(ReservationStrategyType.CONDITIONAL_UPDATE)
        val optimisticResult = runConcurrentReservation(ReservationStrategyType.JPA_OPTIMISTIC_LOCK)
        val pessimisticResult = runConcurrentReservation(ReservationStrategyType.JPA_PESSIMISTIC_LOCK)

        println("conditional update result: $conditionalUpdateResult")
        println("optimistic lock result: $optimisticResult")
        println("pessimistic lock result: $pessimisticResult")

        assertThat(conditionalUpdateResult.successCount).isEqualTo(100)
        assertThat(conditionalUpdateResult.failureCount).isEqualTo(100)
        assertThat(conditionalUpdateResult.finalAllocatedQuantity).isEqualTo(100)
        assertThat(conditionalUpdateResult.finalAvailableQuantity).isEqualTo(0)

        assertThat(optimisticResult.successCount).isLessThanOrEqualTo(100)
        assertThat(optimisticResult.successCount + optimisticResult.failureCount).isEqualTo(200)
        assertThat(optimisticResult.finalAllocatedQuantity).isEqualTo(optimisticResult.successCount.toLong())
        assertThat(optimisticResult.finalAvailableQuantity).isEqualTo(100 - optimisticResult.successCount.toLong())

        assertThat(pessimisticResult.successCount).isEqualTo(100)
        assertThat(pessimisticResult.failureCount).isEqualTo(100)
        assertThat(pessimisticResult.finalAllocatedQuantity).isEqualTo(100)
        assertThat(pessimisticResult.finalAvailableQuantity).isEqualTo(0)
    }

    private fun runConcurrentReservation(strategyType: ReservationStrategyType): ConcurrencyResult {
        val stock = stockService.create(
            CreateStockCommand(
                sku = "SKU-$strategyType",
                totalQuantity = 100,
            ),
        )

        val executor = Executors.newFixedThreadPool(32)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(200)
        val successCount = AtomicInteger()
        val failureCount = AtomicInteger()

        val elapsedMillis = measureTimeMillis {
            repeat(200) {
                executor.submit {
                    try {
                        startLatch.await()
                        stockService.reserve(
                            command = ReserveStockCommand(stockId = stock.id, quantity = 1),
                            strategyType = strategyType,
                        )
                        successCount.incrementAndGet()
                    } catch (_: Exception) {
                        failureCount.incrementAndGet()
                    } finally {
                        doneLatch.countDown()
                    }
                }
            }

            startLatch.countDown()
            check(doneLatch.await(30, TimeUnit.SECONDS)) {
                "concurrency test timed out: $strategyType"
            }
        }

        executor.shutdown()

        val finalStock = stockService.get(stock.id)

        return ConcurrencyResult(
            strategyType = strategyType,
            successCount = successCount.get(),
            failureCount = failureCount.get(),
            finalAllocatedQuantity = finalStock.allocatedQuantity,
            finalAvailableQuantity = finalStock.availableQuantity,
            elapsedMillis = elapsedMillis,
        )
    }
}

data class ConcurrencyResult(
    val strategyType: ReservationStrategyType,
    val successCount: Int,
    val failureCount: Int,
    val finalAllocatedQuantity: Long,
    val finalAvailableQuantity: Long,
    val elapsedMillis: Long,
)
