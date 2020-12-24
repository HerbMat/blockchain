package com.crypto.blockchain.rest.controller

import com.crypto.blockchain.domain.NodeService
import com.crypto.blockchain.domain.domain.Block
import com.crypto.blockchain.domain.domain.Transaction
import com.crypto.blockchain.rest.api.NodeGateway
import com.crypto.blockchain.rest.model.ChainResponse
import com.crypto.blockchain.rest.model.RegisterNodeResponse
import com.crypto.blockchain.rest.model.ResolvedConflictsResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.LocalDate

@AutoConfigureWebTestClient
@SpringBootTest
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD, classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class NodeControllerSpec extends Specification {

    @Autowired
    private ObjectMapper objectMapper

    @Autowired
    private WebTestClient webTestClient

    @Autowired
    private NodeService nodeService

    @SpringBean
    private NodeGateway nodeGateway = Mock(NodeGateway)

    def "it registers new node successfully"() {
        when:
            def response = webTestClient
                    .post()
                    .uri({ uriBuilder ->
                        uriBuilder.path("/nodes/register")
                                .queryParam("address", "http://test.com")
                                .build()
                    })
                    .exchange()
                    .returnResult(RegisterNodeResponse)
        then:
         response.status == HttpStatus.CREATED
        and:
            nodeService.nodes.count().block() == 1
            nodeService.nodes.blockFirst() == "http://test.com"
        and:
            verifyAll(response.responseBody.blockFirst()) {
                it.message == "New nodes have been added"
                it.totalNodes == 1
            }
    }

    def "It replaces current chain"() {
        given:
            nodeService.registerNode("test.com")
            def newChain = mockChain()
        when:
            def response = webTestClient
                    .put()
                    .uri("/nodes/resolve")
                    .exchange()
                    .returnResult(ResolvedConflictsResponse)
        then:
            response.status == HttpStatus.OK
        and:
            verifyAll(response.responseBody.blockFirst()) {
                it.message == "Our chain was replaced"
                it.chain == newChain
            }
        and:
            1 * nodeGateway.getExternalChain("test.com") >> Mono.just(new ChainResponse(
                    newChain,
                    newChain.size()
            ))

    }

    def "It is too short to replace current chain"() {
        given:
        nodeService.registerNode("test.com")
        def newChain = mockChain()
        when:
        def response = webTestClient
                .put()
                .uri("/nodes/resolve")
                .exchange()
                .returnResult(ResolvedConflictsResponse)
        then:
        response.status == HttpStatus.OK
        and:
        verifyAll(response.responseBody.blockFirst()) {
            it.message == "Our chain is authoritative"
            it.chain != null
        }
        and:
        1 * nodeGateway.getExternalChain("test.com") >> Mono.just(new ChainResponse(
                List.of(),
                0
        ))

    }

    def "It cannot be replaced because new chain is invalid"() {
        given:
        nodeService.registerNode("test.com")
        def newChain = new ArrayList<Block>(mockChain())
        newChain.add(mockBlockWithHash(3, 20, "1"))
        when:
        def response = webTestClient
                .put()
                .uri("/nodes/resolve")
                .exchange()
                .returnResult(ResolvedConflictsResponse)
        then:
        response.status == HttpStatus.OK
        and:
        verifyAll(response.responseBody.blockFirst()) {
            it.message == "Our chain is authoritative"
            it.chain != null
        }
        and:
        1 * nodeGateway.getExternalChain("test.com") >> Mono.just(new ChainResponse(
                newChain,
                newChain.size()
        ))

    }

    private def mockChain() {
        return List.of(
                mockBlockWithHash(1, 100, "1"),
                mockBlockWithHash(2, 33575, "91f3176e3e37ea0e7ae5bfcfc05e19b4a5f5070b88426f76e35dc7e2a2abfaf2"),
        )
    }

    private def mockBlockWithHash(int index, int proof, String previousHash) {
        return new Block(
                index,
                LocalDate.of(2020, 6, 17),
                new ArrayList<Transaction>(),
                proof,
                previousHash
        )
    }
}
