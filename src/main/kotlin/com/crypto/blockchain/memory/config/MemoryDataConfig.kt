package com.crypto.blockchain.memory.config

import com.crypto.blockchain.domain.domain.Block
import com.crypto.blockchain.domain.domain.Blockchain
import com.crypto.blockchain.domain.domain.Transaction
import com.crypto.blockchain.memory.repository.BlockchainRepository
import com.crypto.blockchain.memory.repository.DefaultBlockchainRepository
import com.crypto.blockchain.memory.repository.DefaultNodeRepository
import com.crypto.blockchain.memory.repository.NodeRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MemoryDataConfig {

    @Bean
    fun blockChainRepository(): BlockchainRepository {
        return DefaultBlockchainRepository(Blockchain(ArrayList<Transaction>(), ArrayList<Block>()))
    }

    @Bean
    fun nodeRepository(): NodeRepository {
        return DefaultNodeRepository(ArrayList<String>())
    }
}