package com.crypto.blockchain.rest.controller

import com.crypto.blockchain.domain.BlockchainService
import com.crypto.blockchain.domain.NodeService
import com.crypto.blockchain.rest.model.RegisterNodeResponse
import com.crypto.blockchain.rest.model.ResolvedConflictsResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.net.Inet4Address

@RestController
@RequestMapping("/nodes")
class NodeController(private val nodeService: NodeService, private val blockchainService: BlockchainService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerNode(@RequestParam address: String): Mono<RegisterNodeResponse> {
        nodeService.registerNode(address)

        return nodeService.nodes.count().map {
            RegisterNodeResponse(
                message = "New nodes have been added",
                totalNodes = it
            )
        }
    }

    @PutMapping("/resolve")
    fun resolveConflicts(): Mono<ResolvedConflictsResponse> {
        return blockchainService.resolveConflicts()
            .flatMap { resolved ->
                blockchainService.getChain().collectList().map {
                    ResolvedConflictsResponse(
                        message = if (resolved) "Our chain was replaced" else "Our chain is authoritative",
                        chain = it
                    )
                }
            }
    }
}