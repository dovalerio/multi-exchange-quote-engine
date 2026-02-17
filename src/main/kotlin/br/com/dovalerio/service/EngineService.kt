package br.com.dovalerio.service

import br.com.dovalerio.model.*

class EngineService(
    private val quoteService: QuoteService,
    private val spreadService: SpreadService,
    private val arbitrageDetector: ArbitrageDetector
) {

    fun execute(quoteCurrency: String): Result<EngineReport> {

        return quoteService.fetchAll(quoteCurrency)
            .map { quotes ->

                val spreads = CryptoAsset.entries.mapNotNull { asset ->

                    val pair = CurrencyPair(asset, quoteCurrency)
                    val pairQuotes = quotes.filter { it.pair == pair.toString() }

                    val spreadResult = spreadService.calculate(pair, pairQuotes)
                        ?: return@mapNotNull null

                    if (arbitrageDetector.detect(spreadResult)) {
                        spreadResult
                    } else {
                        spreadResult
                    }
                }

                val metrics = quoteService.collectMetrics()

                EngineReport(
                    spreads = spreads,
                    metrics = metrics
                )
            }
    }
}
