package com.crypto.blockchain.rest.controller

import com.crypto.blockchain.rest.model.ChainResponse
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
class ChainControllerSpec extends Specification {

    @Autowired
    private ObjectMapper objectMapper

    @Autowired
    private WebTestClient webTestClient

    def "it should return chain"() {
        when:
            def response = webTestClient
                    .get()
                    .uri("/chain")
                    .exchange()
                    .returnResult(ChainResponse)
        then:
            response.status == HttpStatus.OK
        and:
            verifyAll(response.responseBody.blockFirst()) {
                it.length == 1
                it.chain.size() == 1
                def chainElement = it.chain.iterator().next()
                chainElement.proof == 100
                chainElement.index == 1
                chainElement.previousHash == "1"
                chainElement.transactions.empty == true
        }
    }
}
