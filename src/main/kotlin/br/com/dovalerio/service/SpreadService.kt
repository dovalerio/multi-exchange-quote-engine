package br.com.dovalerio.service

import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.Quote
import br.com.dovalerio.model.SpreadResult
import java.math.RoundingMode

class SpreadService {

    fun calculate(pair: CurrencyPair, quotes: List<Quote>): SpreadResult? {

        if (quotes.size < 2) return null

        val bestBuy = quotes.minByOrNull { it.ask } ?: return null
        val bestSell = quotes.maxByOrNull { it.bid } ?: return null

        val spread = bestSell.bid
            .subtract(bestBuy.ask)
            .setScale(2, RoundingMode.HALF_UP)

        return SpreadResult(
            pair = pair,
            bestBuy = bestBuy,
            bestSell = bestSell,
            spread = spread
        )
    }
}
