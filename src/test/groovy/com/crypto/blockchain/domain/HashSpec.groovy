package com.crypto.blockchain.domain

import com.crypto.blockchain.domain.domain.Block
import spock.lang.Specification

import java.time.LocalDate

class HashSpec extends Specification {
    def hash = new Hash()
    def "It should return hash"() {
        given:
            def block = new Block(
                0,
                    LocalDate.of(2020, 6, 9),
                    new ArrayList<>(),
                    0,
                    null
            )
        when:
            def result = hash.createHash(block)
        then:
            result == "7a77873012c4f1b3d7b97711e9d2116ae5a7a596fe051ec5e3ec944f05a7345e"
    }
}
