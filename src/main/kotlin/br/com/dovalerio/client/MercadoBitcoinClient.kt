package br.com.dovalerio.client

import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.Quote
import kotlinx.serialization.Serializable
import java.math.BigDecimal

class MercadoBitcoinClient : BaseExchangeClient("MercadoBitcoin") {

    override fun getQuote(pair: CurrencyPair): Quote {

        val symbol = resolveSymbol(pair)

        val body = executeGet(
            "https://www.mercadobitcoin.net/api/$symbol/ticker/"
        )

        val dto = json.decodeFromString(
            MBTickerResponse.serializer(),
            body
        )

        return Quote(
            exchange = exchangeName,
            pair = pair.toString(),
            bid = BigDecimal(dto.ticker.buy),
            ask = BigDecimal(dto.ticker.sell),
            timestamp = System.currentTimeMillis()
        )
    }

    private fun resolveSymbol(pair: CurrencyPair): String =
        pair.base.name

    @Serializable
    private data class MBTickerResponse(
        val ticker: Ticker
    )

    @Serializable
    private data class Ticker(
        val buy: String,
        val sell: String
    )
}
