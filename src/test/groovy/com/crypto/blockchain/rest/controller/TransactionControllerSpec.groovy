package com.crypto.blockchain.rest.controller


import com.crypto.blockchain.domain.domain.Transaction
import com.crypto.blockchain.memory.repository.BlockchainRepository
import com.crypto.blockchain.rest.model.TransactionCreated
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import spock.lang.Specification

@AutoConfigureWebTestClient
@DirtiesContext
@SpringBootTest
class TransactionControllerSpec extends Specification {

    @Autowired
    private ObjectMapper objectMapper

    @Autowired
    private WebTestClient webTestClient

    @Autowired
    private BlockchainRepository blockchainRepository

    def "it creates new transaction"() {
        given:
            def transactionRequestBody = new Transaction(
                   "sender",
                    "recipient",
                    BigDecimal.ONE
            )
        when:
            def response = webTestClient
                    .post()
                    .uri("/transactions")
                    .body(BodyInserters.fromValue(transactionRequestBody))
                    .exchange()
                    .returnResult(TransactionCreated)

        then:
            response.status == HttpStatus.CREATED
        and:
            verifyAll(response.responseBody.blockFirst()) {
                it.message == "Transaction will be added to Block 2"
            }
        and:
            verifyAll(blockchainRepository.getCurrentTransactions()) {
                it.size() == 1
                def transaction = it.iterator().next()
                transaction.amount == BigDecimal.ONE
                transaction.sender == "sender"
                transaction.recipient == "recipient"
            }
    }
}
