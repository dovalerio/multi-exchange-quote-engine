package br.com.dovalerio.service

import br.com.dovalerio.aggregator.QuoteAggregator
import br.com.dovalerio.client.ExchangeClient
import br.com.dovalerio.model.Quote

class QuoteService(
    private val aggregator: QuoteAggregator,
    private val clients: List<ExchangeClient>
) {

    fun fetchAll(quoteCurrency: String): Result<List<Quote>> =
        aggregator.getAllQuotes(quoteCurrency)

    fun collectMetrics() =
        clients.map { it.metrics() }
}
