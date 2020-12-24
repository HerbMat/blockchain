package com.crypto.blockchain.rest.controller

import com.crypto.blockchain.domain.BlockchainService
import com.crypto.blockchain.domain.Hash
import com.crypto.blockchain.domain.ProofService
import com.crypto.blockchain.rest.model.BlockCreated
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.math.BigDecimal

@RestController
@RequestMapping("/mine")
class MineController(private val proofService: ProofService, val blockchainService: BlockchainService) {

    @PutMapping
    fun mine(): Mono<BlockCreated> {
        return blockchainService
            .getLastBlock()
            .map {
                val proof = proofService.proofOfWork(lastProof = it.proof)
                blockchainService.newTransaction(
                    sender = "0",
                    recipient = "thisNode",
                    amount = BigDecimal.ONE
                )
                Pair(proof, Hash.createHash(it))
            }
            .flatMap { blockchainService.newBlock(it.first, it.second) }
            .map {
                BlockCreated(
                    message = "New Block Forged",
                    index = it.index,
                    transactions = it.transactions,
                    proof = it.proof,
                    previousHash = it.previousHash!!
                )
            }
    }
}