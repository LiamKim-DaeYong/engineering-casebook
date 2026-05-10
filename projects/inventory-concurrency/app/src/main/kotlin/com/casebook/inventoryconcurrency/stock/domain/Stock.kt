package com.casebook.inventoryconcurrency.stock.domain

import com.casebook.inventoryconcurrency.stock.application.StockResult
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(name = "stocks")
class Stock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val sku: String,

    @Column(nullable = false)
    var totalQuantity: Long,

    @Column(nullable = false)
    var allocatedQuantity: Long = 0,

    @Version
    var version: Long = 0,
) {
    val availableQuantity: Long
        get() = totalQuantity - allocatedQuantity

    fun allocate(quantity: Long) {
        require(quantity > 0) { "quantity must be positive" }
        check(availableQuantity >= quantity) { "not enough stock" }

        allocatedQuantity += quantity
    }
}

fun Stock.toResult(): StockResult {
    return StockResult(
        id = id,
        sku = sku,
        totalQuantity = totalQuantity,
        allocatedQuantity = allocatedQuantity,
        availableQuantity = availableQuantity,
    )
}
