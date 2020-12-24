package com.crypto.blockchain.domain.domain

data class Blockchain(
    var currentTransactions: MutableCollection<Transaction>,
    var chain: MutableCollection<Block>
)