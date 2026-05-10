package com.casebook.inventoryconcurrency.stock.domain

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface StockRepository : JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): Stock?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update Stock s
        set s.allocatedQuantity = s.allocatedQuantity + :quantity
        where s.id = :id
          and s.totalQuantity - s.allocatedQuantity >= :quantity
        """,
    )
    fun allocateIfAvailable(
        @Param("id") id: Long,
        @Param("quantity") quantity: Long,
    ): Int
}
