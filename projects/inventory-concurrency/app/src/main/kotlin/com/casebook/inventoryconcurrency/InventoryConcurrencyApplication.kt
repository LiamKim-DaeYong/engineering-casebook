package com.casebook.inventoryconcurrency

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InventoryConcurrencyApplication

fun main(args: Array<String>) {
    runApplication<InventoryConcurrencyApplication>(*args)
}
