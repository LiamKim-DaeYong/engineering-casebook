package com.casebook.inventoryconcurrency.stock.api

import com.casebook.inventoryconcurrency.stock.application.CreateStockCommand
import com.casebook.inventoryconcurrency.stock.application.ReservationResult
import com.casebook.inventoryconcurrency.stock.application.ReservationStrategyType
import com.casebook.inventoryconcurrency.stock.application.ReserveStockCommand
import com.casebook.inventoryconcurrency.stock.application.StockResult
import com.casebook.inventoryconcurrency.stock.application.StockService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stocks")
class StockController(
    private val stockService: StockService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateStockRequest): StockResponse {
        return stockService.create(request.toCommand()).toResponse()
    }

    @GetMapping("/{stockId}")
    fun get(@PathVariable stockId: Long): StockResponse {
        return stockService.get(stockId).toResponse()
    }

    @PostMapping("/{stockId}/reservations/optimistic-lock")
    fun reserveWithOptimisticLock(
        @PathVariable stockId: Long,
        @Valid @RequestBody request: ReserveStockRequest,
    ): ReservationResponse {
        return stockService.reserve(
            command = request.toCommand(stockId),
            strategyType = ReservationStrategyType.JPA_OPTIMISTIC_LOCK,
        ).toResponse()
    }

    @PostMapping("/{stockId}/reservations/pessimistic-lock")
    fun reserveWithPessimisticLock(
        @PathVariable stockId: Long,
        @Valid @RequestBody request: ReserveStockRequest,
    ): ReservationResponse {
        return stockService.reserve(
            command = request.toCommand(stockId),
            strategyType = ReservationStrategyType.JPA_PESSIMISTIC_LOCK,
        ).toResponse()
    }

    @PostMapping("/{stockId}/reservations/conditional-update")
    fun reserveWithConditionalUpdate(
        @PathVariable stockId: Long,
        @Valid @RequestBody request: ReserveStockRequest,
    ): ReservationResponse {
        return stockService.reserve(
            command = request.toCommand(stockId),
            strategyType = ReservationStrategyType.CONDITIONAL_UPDATE,
        ).toResponse()
    }

    @PostMapping("/{stockId}/reservations/redis-lua")
    fun reserveWithRedisLua(
        @PathVariable stockId: Long,
        @Valid @RequestBody request: ReserveStockRequest,
    ): ReservationResponse {
        return stockService.reserve(
            command = request.toCommand(stockId),
            strategyType = ReservationStrategyType.REDIS_LUA,
        ).toResponse()
    }
}

data class CreateStockRequest(
    @field:NotBlank
    val sku: String,

    @field:Min(1)
    val totalQuantity: Long,
) {
    fun toCommand(): CreateStockCommand {
        return CreateStockCommand(
            sku = sku,
            totalQuantity = totalQuantity,
        )
    }
}

data class ReserveStockRequest(
    @field:Min(1)
    val quantity: Long,
) {
    fun toCommand(stockId: Long): ReserveStockCommand {
        return ReserveStockCommand(
            stockId = stockId,
            quantity = quantity,
        )
    }
}

data class StockResponse(
    val id: Long,
    val sku: String,
    val totalQuantity: Long,
    val allocatedQuantity: Long,
    val availableQuantity: Long,
)

data class ReservationResponse(
    val stockId: Long,
    val quantity: Long,
    val strategy: ReservationStrategyType,
    val reserved: Boolean,
)

fun StockResult.toResponse(): StockResponse {
    return StockResponse(
        id = id,
        sku = sku,
        totalQuantity = totalQuantity,
        allocatedQuantity = allocatedQuantity,
        availableQuantity = availableQuantity,
    )
}

fun ReservationResult.toResponse(): ReservationResponse {
    return ReservationResponse(
        stockId = stockId,
        quantity = quantity,
        strategy = strategyType,
        reserved = true,
    )
}
