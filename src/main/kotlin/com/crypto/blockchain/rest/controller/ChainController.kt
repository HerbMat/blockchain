package com.crypto.blockchain.rest.controller

import com.crypto.blockchain.domain.BlockchainService
import com.crypto.blockchain.rest.model.ChainResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/chain")
class ChainController(private val blockchainService: BlockchainService) {

    @GetMapping
    fun getChain(): Mono<ChainResponse> {
        return blockchainService.getChain().collectList()
            .map { ChainResponse(
                chain = it,
                length = it.size
            )}
    }
}