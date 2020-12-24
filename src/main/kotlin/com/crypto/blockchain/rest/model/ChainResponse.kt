package com.crypto.blockchain.rest.model

import com.crypto.blockchain.domain.domain.Block

data class ChainResponse(val chain: Collection<Block>, val length: Int)