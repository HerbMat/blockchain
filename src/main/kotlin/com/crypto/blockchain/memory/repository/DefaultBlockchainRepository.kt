package com.crypto.blockchain.memory.repository

import com.crypto.blockchain.domain.domain.Block
import com.crypto.blockchain.domain.domain.Blockchain
import com.crypto.blockchain.domain.domain.Transaction
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class DefaultBlockchainRepository(private val blockchain: Blockchain): BlockchainRepository {
    override fun addBlock(block: Block): Mono<Block> {
        blockchain.chain.add(block)
        blockchain.currentTransactions = ArrayList<Transaction>()

        return Mono.just(block)
    }

    override fun addTransaction(transaction: Transaction): Mono<Transaction> {
        blockchain.currentTransactions.add(transaction)

        return Mono.just(transaction)
    }

    override fun getCurrentTransactions(): Collection<Transaction> {
        return blockchain.currentTransactions
    }

    override fun getCurrentTransactionIndex(): Mono<Int> {
        return Mono.just(blockchain.chain.size + 1)
    }

    override fun getLastBlock(): Mono<Block> {
        return Mono.justOrEmpty(blockchain.chain.last())
    }

    override fun getChain(): Flux<Block> {
        return Flux.fromIterable(blockchain.chain)
    }

    override fun setChain(newChain: MutableCollection<Block>) {
        blockchain.chain = newChain
    }
}