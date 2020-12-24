package com.crypto.blockchain.domain.domain

import java.math.BigDecimal

data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: BigDecimal
)