package com.crypto.blockchain.memory.repository

import reactor.core.publisher.Flux

interface NodeRepository {
    fun addNode(node: String): String
    fun getNodes(): Flux<String>
}