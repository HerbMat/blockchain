package com.crypto.blockchain.rest.api

import com.crypto.blockchain.rest.model.ChainResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class NodeGateway(private val webClient: WebClient) {
    fun getExternalChain(address: String) : Mono<ChainResponse> {
        return webClient
            .get()
            .uri("$address/chain")
            .exchange()
            .flatMap { clientResponse -> clientResponse.toEntity(ChainResponse::class.java).map { responseEntity -> responseEntity.body } }
    }
}