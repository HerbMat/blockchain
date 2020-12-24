package com.crypto.blockchain.domain

import com.crypto.blockchain.memory.repository.NodeRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class NodeService(private val nodeRepository: NodeRepository) {

    val nodes: Flux<String>
        get() {
            return nodeRepository.getNodes()
        }

    fun registerNode(address: String) {
        nodeRepository.addNode(address)
    }


}