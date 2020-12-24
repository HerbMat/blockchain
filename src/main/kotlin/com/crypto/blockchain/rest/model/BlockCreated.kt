package com.crypto.blockchain.rest.model

import com.crypto.blockchain.domain.domain.Transaction

data class BlockCreated(
    val message: String,
    val index: Int,
    val transactions: Collection<Transaction>,
    val proof: Int,
    val previousHash: String
)