package com.crypto.blockchain.domain

import org.springframework.stereotype.Service

@Service
class ProofService {
    companion object InitialProofConst {
        const val INITIAL_PROOF = 0
        const val NUMBER_OF_LAST_CHARACTERS = 4
        const val LAST_CHARACTERS_MATCH = "0000"
    }

    fun proofOfWork(lastProof: Int): Int {
        var proof: Int = INITIAL_PROOF
        while (!validateProof(lastProof, proof)) {
            proof += 1
        }
        return proof
    }

    fun validateProof(lastProof: Int, proof: Int): Boolean {
        val guessHash: String = Hash.createHash("${lastProof}${proof}")
        return guessHash.takeLast(NUMBER_OF_LAST_CHARACTERS) == LAST_CHARACTERS_MATCH
    }
}