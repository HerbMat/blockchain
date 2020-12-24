package com.crypto.blockchain.domain

import com.crypto.blockchain.domain.domain.Block
import com.crypto.blockchain.domain.domain.Transaction
import com.crypto.blockchain.memory.repository.BlockchainRepository
import com.crypto.blockchain.rest.api.NodeGateway
import com.crypto.blockchain.rest.model.ChainResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Service
class BlockchainService(
    private val proofService: ProofService,
    private val nodeService: NodeService,
    private val blockchainRepository: BlockchainRepository,
    private val nodeGateway: NodeGateway
) {


    fun newBlock(proof: Int, previousHash: String?): Mono<Block> {
        val previousHashMono = Mono.justOrEmpty(previousHash)
            .switchIfEmpty(Mono.defer {
                blockchainRepository.getLastBlock()
                    .map { Hash.createHash(it) }
            })

        return Mono.zip(blockchainRepository.getCurrentTransactionIndex(), previousHashMono)
            .map {
                Block(
                    index = it.t1,
                    timestamp = LocalDate.now(),
                    transactions = blockchainRepository.getCurrentTransactions(),
                    proof = proof,
                    previousHash = it.t2
                )
            }
            .flatMap {
                blockchainRepository.addBlock(it)
            }
    }

    fun newTransaction(sender: String, recipient: String, amount: BigDecimal): Mono<Int> {
        blockchainRepository.addTransaction(
            Transaction(
                sender = sender,
                recipient = recipient,
                amount = amount
            )
        )
        return blockchainRepository.getCurrentTransactionIndex()
    }

    fun validateChain(chain: Collection<Block>): Boolean {
        var lastBlock = chain.first()
        for (block in chain.drop(1)) {
            if (block.previousHash != Hash.createHash(lastBlock)) {
                return false
            }
            if (!(proofService.validateProof(lastBlock.proof, block.proof))) {
                return false
            }
            lastBlock = block
        }
        return true
    }

    fun resolveConflicts(): Mono<Boolean> {
        return Flux.merge(
            nodeService.nodes.flatMap {
                nodeGateway.getExternalChain(it)
                    .flatMap { chainResponse -> resolveOne(chainResponse) }
            }
        ).reduce { firstValue, secondValue -> firstValue || secondValue }
    }

    fun getLastBlock(): Mono<Block> {
        return blockchainRepository.getLastBlock()
    }

    fun getChain(): Flux<Block> {
        return blockchainRepository.getChain()
    }

    @Synchronized
    private fun resolveOne(chainResponse: ChainResponse): Mono<Boolean> {
        return blockchainRepository.getChain()
            .count()
            .map { tryToReplaceOldChain(chainResponse, it) }

    }

    private fun tryToReplaceOldChain(candidateChainResponse: ChainResponse, currentChainLength: Long): Boolean {
        if (isChainShouldBeReplaced(candidateChainResponse, currentChainLength)) {
            blockchainRepository.setChain(ArrayList<Block>(candidateChainResponse.chain))

            return true
        }
        return false
    }

    private fun isChainShouldBeReplaced(candidateChainResponse: ChainResponse, chainLength: Long): Boolean {
        return candidateChainResponse.length > chainLength && validateChain(candidateChainResponse.chain)
    }
}