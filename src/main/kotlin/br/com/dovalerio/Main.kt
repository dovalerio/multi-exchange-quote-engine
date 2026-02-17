package br.com.dovalerio

import br.com.dovalerio.aggregator.QuoteAggregator
import br.com.dovalerio.client.BinanceClient
import br.com.dovalerio.client.MercadoBitcoinClient
import br.com.dovalerio.service.ArbitrageDetector
import br.com.dovalerio.service.EngineService
import br.com.dovalerio.service.QuoteService
import br.com.dovalerio.service.SpreadService

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

    engineService.execute("BRL")
        .onSuccess { report ->

            println("======================================")
            println("MULTI-EXCHANGE QUOTE ENGINE")
            println("======================================")

            report.spreads.forEach { result ->

                println()
                println("Pair: ${result.pair}")
                println("----------------------------------")

                println("Best BUY  : ${result.bestBuy.exchange} @ ${result.bestBuy.ask}")
                println("Best SELL : ${result.bestSell.exchange} @ ${result.bestSell.bid}")
                println("Spread    : ${result.spread}")

                if (result.hasArbitrage) {
                    println("⚡ Arbitrage opportunity detected")
                }
            }

            println()
            println("======================================")
            println("EXCHANGE METRICS")
            println("======================================")

            report.metrics.forEach { metric ->
                println(
                    "${metric.exchange} | success=${metric.successCount} | " +
                            "failure=${metric.failureCount} | breaker=${metric.breakerState}"
                )
            }
        }
        .onFailure { error ->
            println("❌ Engine failed: ${error.message}")
        }
}
