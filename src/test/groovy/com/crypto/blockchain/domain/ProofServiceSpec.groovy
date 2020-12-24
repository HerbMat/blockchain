package com.crypto.blockchain.domain

import spock.lang.Specification
import spock.lang.Unroll

class ProofServiceSpec extends Specification {
    def proofService = new ProofService()

    def "It should create proof"() {
        given:
            def lastProof = 100
        when:
            def proof = proofService.proofOfWork(lastProof)
        then:
            proof == 33575
    }

    @Unroll
    def "It should validate proof successfully"() {
        when:
            def result = proofService.validateProof(lastProof, proof)
        then:
            result == expectedResult
        where:
            proof | lastProof | expectedResult
            33575 | 100       | true
            100   | 100       | false
    }
}
