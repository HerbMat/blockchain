package com.crypto.blockchain.rest.controller


import com.crypto.blockchain.rest.model.BlockCreated
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@AutoConfigureWebTestClient
@SpringBootTest
@DirtiesContext
class MineControllerSpec extends Specification {

    @Autowired
    private ObjectMapper objectMapper

    @Autowired
    private WebTestClient webTestClient

    def "it mines new block"() {
        when:
            def response = webTestClient
                    .put()
                    .uri("/mine")
                    .exchange()
                    .returnResult(BlockCreated)
        then:
            response.status == HttpStatus.OK
        and:
            verifyAll(response.responseBody.blockFirst()) {
                it.message == "New Block Forged"
                it.index == 2
                it.proof ==  33575
                it.previousHash == '622cf934a0b6b32fa5a89490df46beac2d1be54560656ec8e658a08248820ab0'
                it.transactions.size() == 1
                def transaction = it.transactions.iterator().next()
                transaction.sender == "0"
                transaction.recipient == "thisNode"
                transaction.amount == BigDecimal.ONE
            }
    }
}
