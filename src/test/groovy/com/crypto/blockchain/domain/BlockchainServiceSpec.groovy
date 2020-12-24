package com.crypto.blockchain.domain

import com.crypto.blockchain.domain.domain.Block
import com.crypto.blockchain.domain.domain.Transaction
import com.crypto.blockchain.memory.repository.BlockchainRepository
import com.crypto.blockchain.rest.api.NodeGateway
import com.crypto.blockchain.rest.model.ChainResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.LocalDate

class BlockchainServiceSpec extends Specification {
    BlockchainService blockchainService;
    BlockchainRepository blockchainRepository;
    ProofService proofService;
    NodeService nodeService;
    NodeGateway nodeGateway;

    def setup() {
        blockchainRepository = Mock(BlockchainRepository)
        proofService = Mock(ProofService)
        nodeService = Mock(NodeService)
        nodeGateway = Mock(NodeGateway)
        blockchainService = new BlockchainService(proofService, nodeService, blockchainRepository, nodeGateway)
    }

    def 'It should create new Transaction'() {
        given:
            blockchainRepository.getCurrentTransactionIndex() >> Mono.just(2)
        when:
            def result = blockchainService.newTransaction(
                    'sender',
                    'recipient',
                    BigDecimal.ONE
            )
        then:
            result.block() == 2
        and:
            1 * blockchainRepository.addTransaction(_ as Transaction) >> {
                    Transaction transaction = it[0] as Transaction
                    assert transaction.sender == 'sender'
                    assert transaction.amount == BigDecimal.ONE
                    assert transaction.recipient == 'recipient'
            }
    }

    def 'It should create new Block'() {
        given:
            def currentTransactions = List.of()
            def lastBlock = mockBlockWithHash(1, "3")
            blockchainRepository.getCurrentTransactions() >> currentTransactions
            blockchainRepository.getCurrentTransactionIndex() >> Mono.just(2)
            blockchainRepository.getLastBlock() >> Mono.just(lastBlock)
        when:
            def result = blockchainService.newBlock(150, "f828f1b429d943b829b38a24826f8e3adaa14b554e2427eb675528387fac7ebd")
                .block()
        then:
            result != null
        and:
            1 * blockchainRepository.addBlock(_ as Block) >> {
                Block block = it[0] as Block
                assert block.index == 2
                assert block.transactions != null
                assert block.timestamp != null
                assert block.transactions == currentTransactions
                assert block.proof == 150
                assert block.previousHash == "f828f1b429d943b829b38a24826f8e3adaa14b554e2427eb675528387fac7ebd"

                return Mono.just(block)
            }
    }

    def 'Chain validation should pass'() {
        given:
            proofService.validateProof(_ , _) >> true
            def chain = mockChain()
        when:
            def result = blockchainService.validateChain(chain)
        then:
            result == true
    }

    def 'Chain validation should fail by using bad hash'() {
        given:
            proofService.validateProof(_ , _) >> true
            def chain = new ArrayList(mockChain())
            chain.add(mockBlockWithHash(5, "2"))
        when:
            def result = blockchainService.validateChain(chain)
        then:
            result == false
    }

    def 'Chain validation should fail by using bad proof'() {
        given:
            proofService.validateProof(_ , _) >> false
            def chain = mockChain()
        when:
            def result = blockchainService.validateChain(chain)
        then:
            result == false
    }

    def 'Chain was replaced'() {
        given:
            def chain = mockChain()
            def replacingChain = new ArrayList<Block>(mockChain())
            replacingChain.add(mockBlockWithHash(7, "f371a3f348099574d45cb616429ba02a52512b9ae6d66078afe577087baec2ab"))
            nodeService.nodes >> Flux.fromIterable(mockNodes())
            nodeGateway.getExternalChain("test.com") >> Mono.just(new ChainResponse(chain, chain.size()))
            nodeGateway.getExternalChain("test2.com") >> Mono.just(new ChainResponse(replacingChain, replacingChain.size()))
            nodeGateway.getExternalChain("test3.com") >> Mono.just(new ChainResponse(chain, chain.size()))
            blockchainRepository.getChain() >> Flux.fromIterable(chain)
            proofService.validateProof(_, _) >> true
        when:
            def result = blockchainService.resolveConflicts().block()
        then:
            result == true
        and:
            1 * blockchainRepository.setChain({ assert it == replacingChain  })
    }

    def 'Chain was not replaced because of bad hash'() {
        given:
            def chain = mockChain()
            def replacingChain = new ArrayList<Block>(mockChain())
            replacingChain.add(mockBlockWithHash(7, "2"))
            nodeService.nodes >> Flux.fromIterable(mockNodes())
            nodeGateway.getExternalChain("test.com") >> Mono.just(new ChainResponse(chain, chain.size()))
            nodeGateway.getExternalChain("test2.com") >> Mono.just(new ChainResponse(replacingChain, replacingChain.size()))
            nodeGateway.getExternalChain("test3.com") >> Mono.just(new ChainResponse(chain, chain.size()))
            blockchainRepository.getChain() >> Flux.fromIterable(chain)
            proofService.validateProof(_, _) >> true
        when:
          def result = blockchainService.resolveConflicts().block()
        then:
          result == false
        and:
          0 * blockchainRepository.setChain(_)
    }

    def 'Chain was not replaced because of bad proof'() {
        given:
            def chain = mockChain()
            def replacingChain = new ArrayList<Block>(mockChain())
            replacingChain.add(mockBlockWithHash(7, "2"))
            nodeService.nodes >> Flux.fromIterable(mockNodes())
            nodeGateway.getExternalChain("test.com") >> Mono.just(new ChainResponse(chain, chain.size()))
            nodeGateway.getExternalChain("test2.com") >> Mono.just(new ChainResponse(replacingChain, replacingChain.size()))
            nodeGateway.getExternalChain("test3.com") >> Mono.just(new ChainResponse(chain, chain.size()))
            blockchainRepository.getChain() >> Flux.fromIterable(chain)
            proofService.validateProof(_, _) >> false
        when:
            def result = blockchainService.resolveConflicts().block()
        then:
            result == false
        and:
            0 * blockchainRepository.setChain(_)
    }

    def 'Chain was not replaced by smaller one'() {
        given:
            def chain = mockChain()
            def replacingChain = new ArrayList<Block>(mockChain())
            replacingChain.add(mockBlockWithHash(7, "f371a3f348099574d45cb616429ba02a52512b9ae6d66078afe577087baec2ab"))
            nodeService.nodes >> Flux.fromIterable(mockNodes())
            nodeGateway.getExternalChain("test.com") >> Mono.just(new ChainResponse(chain, chain.size()))
            nodeGateway.getExternalChain("test2.com") >> Mono.just(new ChainResponse(chain, chain.size()))
            nodeGateway.getExternalChain("test3.com") >> Mono.just(new ChainResponse(chain, chain.size()))
            blockchainRepository.getChain() >> Flux.fromIterable(replacingChain)
            proofService.validateProof(_, _) >> true
        when:
            def result = blockchainService.resolveConflicts().block()
        then:
            result == false
        and:
            0 * blockchainRepository.setChain(_)
    }

    private def mockChain() {
        return List.of(
                mockBlockWithHash(1, "1"),
                mockBlockWithHash(2, "91f3176e3e37ea0e7ae5bfcfc05e19b4a5f5070b88426f76e35dc7e2a2abfaf2"),
                mockBlockWithHash(3, "3da1a6011c708aefec8ece24b3e6add98d11b3fee682f1d8917605ed051e5017"),
                mockBlockWithHash(4, "1a9b21430ffcce8bd6613cc155b8f7758405e67603c55dcd94866cdc915ddf61"),
        )
    }

    private def mockBlockWithHash(int index, String previousHash) {
        return new Block(
                index,
                LocalDate.of(2020, 6, 17),
                new ArrayList<Transaction>(),
                100,
                previousHash
        )
    }

    private def mockNodes() {
        return List.of(
                "test.com",
                "test2.com",
                "test3.com"
        )
    }
}
