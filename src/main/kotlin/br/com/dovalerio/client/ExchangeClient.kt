package br.com.dovalerio.client

import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.ExchangeMetrics
import br.com.dovalerio.model.Quote

interface ExchangeClient {

    fun getQuote(pair: CurrencyPair): Quote

    fun metrics(): ExchangeMetrics
}
