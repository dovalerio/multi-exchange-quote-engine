package br.com.dovalerio.aggregator

import br.com.dovalerio.client.ExchangeClient
import br.com.dovalerio.model.CryptoAsset
import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.Quote
import java.util.concurrent.Executors

class QuoteAggregator(
    private val clients: List<ExchangeClient>
) {

    fun getAllQuotes(quoteCurrency: String): Result<List<Quote>> {

        return runCatching {

            Executors.newVirtualThreadPerTaskExecutor().use { executor ->

                val futures = CryptoAsset.entries.flatMap { asset ->

                    val pair = CurrencyPair(asset, quoteCurrency)

                    clients.map { client ->
                        executor.submit<Result<Quote>> {
                            runCatching {
                                client.getQuote(pair)
                            }
                        }
                    }
                }

                futures.mapNotNull { future ->
                    val result: Result<Quote> = future.get()

                    result.onFailure { ex ->
                        println("❌ ${ex.javaClass.simpleName}: ${ex.message}")
                    }

                    result.getOrNull()
                }
            }
        }
    }
}
