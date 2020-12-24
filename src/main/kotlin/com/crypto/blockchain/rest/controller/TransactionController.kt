package com.crypto.blockchain.rest.controller;

import com.crypto.blockchain.domain.BlockchainService
import com.crypto.blockchain.domain.domain.Transaction
import com.crypto.blockchain.rest.model.TransactionCreated
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/transactions")
class TransactionController(private val blockchainService: BlockchainService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun newTransaction(@RequestBody transaction: Transaction): Mono<TransactionCreated> {
        return blockchainService.newTransaction(transaction.sender, transaction.recipient, transaction.amount)
            .map { TransactionCreated("Transaction will be added to Block $it") }
    }

}
