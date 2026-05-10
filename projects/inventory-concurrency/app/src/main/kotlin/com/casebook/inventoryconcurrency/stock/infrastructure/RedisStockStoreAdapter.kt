package com.casebook.inventoryconcurrency.stock.infrastructure

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisStockStoreAdapter(
    private val redisTemplate: StringRedisTemplate,
) {

    fun initializeIfAbsent(stockId: Long, totalQuantity: Long) {
        redisTemplate.opsForValue().setIfAbsent(key(stockId), totalQuantity.toString())
    }

    fun restore(stockId: Long, quantity: Long) {
        redisTemplate.opsForValue().increment(key(stockId), quantity)
    }

    fun key(stockId: Long): String {
        return "inventory-concurrency:stock:$stockId:available"
    }
}
