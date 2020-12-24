package com.crypto.blockchain.rest.model

import com.crypto.blockchain.domain.domain.Block

data class ResolvedConflictsResponse (
    val message: String,
    val chain: Collection<Block>
)