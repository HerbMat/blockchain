package com.crypto.blockchain.domain.domain

import java.time.LocalDate

data class Block(
    val index: Int,
    val timestamp: LocalDate,
    val transactions: Collection<Transaction>,
    val proof: Int,
    val previousHash: String?
)