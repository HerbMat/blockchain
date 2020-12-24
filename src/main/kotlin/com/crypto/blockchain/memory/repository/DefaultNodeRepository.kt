package com.crypto.blockchain.memory.repository

import reactor.core.publisher.Flux

class DefaultNodeRepository(private val nodes: MutableCollection<String>): NodeRepository {
    override fun addNode(node: String): String {
        nodes.add(node)

        return node
    }

    override fun getNodes(): Flux<String> {
        return Flux.fromIterable(nodes)
    }

}