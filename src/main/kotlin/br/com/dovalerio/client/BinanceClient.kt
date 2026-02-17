package br.com.dovalerio.client

import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.Quote
import kotlinx.serialization.Serializable
import java.math.BigDecimal

class BinanceClient : BaseExchangeClient("Binance") {

    override fun getQuote(pair: CurrencyPair): Quote {

        val symbol = resolveSymbol(pair)

        val body = executeGet(
            "https://api.binance.com/api/v3/ticker/bookTicker?symbol=$symbol"
        )

        val dto = json.decodeFromString(
            BinanceTicker.serializer(),
            body
        )

        return Quote(
            exchange = exchangeName,
            pair = pair.toString(),
            bid = BigDecimal(dto.bidPrice),
            ask = BigDecimal(dto.askPrice),
            timestamp = System.currentTimeMillis()
        )
    }

    private fun resolveSymbol(pair: CurrencyPair): String =
        "${pair.base.name}${pair.quote}"

    @Serializable
    private data class BinanceTicker(
        val bidPrice: String,
        val askPrice: String
    )
}
