package com.crypto.blockchain.memory.repository

import com.crypto.blockchain.domain.domain.Block
import com.crypto.blockchain.domain.domain.Transaction
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface BlockchainRepository {
    fun addBlock(block: Block): Mono<Block>
    fun addTransaction(transaction: Transaction): Mono<Transaction>
    fun getCurrentTransactions(): Collection<Transaction>
    fun getCurrentTransactionIndex(): Mono<Int>
    fun getLastBlock(): Mono<Block>
    fun getChain(): Flux<Block>
    fun setChain(newChain: MutableCollection<Block>)
}