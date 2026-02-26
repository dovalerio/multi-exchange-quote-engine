package br.com.dovalerio

import br.com.dovalerio.aggregator.QuoteAggregator
import br.com.dovalerio.client.BinanceClient
import br.com.dovalerio.client.MercadoBitcoinClient
import br.com.dovalerio.model.CryptoPriceEvent
import br.com.dovalerio.model.SpreadEvent
import br.com.dovalerio.service.ArbitrageDetector
import br.com.dovalerio.service.EngineService
import br.com.dovalerio.service.QuoteService
import br.com.dovalerio.service.SpreadService
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {

    val clients = listOf(
        BinanceClient(),
        MercadoBitcoinClient()
    )

    val aggregator = QuoteAggregator(clients)

    val quoteService = QuoteService(
        aggregator = aggregator,
        clients = clients
    )

    val spreadService = SpreadService()
    val arbitrageDetector = ArbitrageDetector()

    val engineService = EngineService(
        quoteService = quoteService,
        spreadService = spreadService,
        arbitrageDetector = arbitrageDetector
    )

    val producer = KafkaCryptoProducer("localhost:9092")

    val scheduler = Executors.newSingleThreadScheduledExecutor()
    val virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Encerrando aplicação...")
        scheduler.shutdown()
        virtualExecutor.shutdown()
        producer.close()
    })

    scheduler.scheduleAtFixedRate({

        virtualExecutor.submit {

            val now = Instant.now()
            println("Iniciando ciclo em $now")

            engineService.execute("BRL")
                .onSuccess { report ->

                    report.spreads.forEach { result ->

                        val timestamp = Instant.now().toEpochMilli()

                        val buyEvent = CryptoPriceEvent(
                            symbol = result.pair,
                            exchange = result.bestBuy.exchange,
                            price = result.bestBuy.ask,
                            timestamp = timestamp
                        )

                        val sellEvent = CryptoPriceEvent(
                            symbol = result.pair,
                            exchange = result.bestSell.exchange,
                            price = result.bestSell.bid,
                            timestamp = timestamp
                        )

                        producer.sendCryptoPrice(buyEvent)
                        producer.sendCryptoPrice(sellEvent)

                        val spreadEvent = SpreadEvent(
                            pair = result.pair,
                            bestBuyExchange = result.bestBuy.exchange,
                            bestBuyPrice = result.bestBuy.ask,
                            bestSellExchange = result.bestSell.exchange,
                            bestSellPrice = result.bestSell.bid,
                            spread = result.spread,
                            hasArbitrage = result.hasArbitrage,
                            timestamp = timestamp
                        )

                        producer.sendSpread(spreadEvent)
                    }

                    println("Eventos publicados com sucesso.")
                }
                .onFailure { error ->
                    println("❌ Engine failed: ${error.message}")
                }
        }

    }, 0, 60, TimeUnit.MINUTES)
}