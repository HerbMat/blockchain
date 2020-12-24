package com.crypto.blockchain.config.listener

import com.crypto.blockchain.domain.BlockchainService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class InitBlockchainListener(val blockchainService: BlockchainService): ApplicationListener<ApplicationReadyEvent> {

    companion object InitialBlock {
        const val PREVIOUS_HASH = "1"
        const val PROOF = 100
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        blockchainService.newBlock(previousHash = PREVIOUS_HASH, proof = PROOF)
            .subscribe()
    }
}