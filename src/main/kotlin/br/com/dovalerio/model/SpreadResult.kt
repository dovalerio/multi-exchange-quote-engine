package br.com.dovalerio.model

import java.math.BigDecimal

data class SpreadResult(
    val pair: CurrencyPair,
    val bestBuy: Quote,
    val bestSell: Quote,
    val spread: BigDecimal
) {
    val hasArbitrage: Boolean
        get() = spread > BigDecimal.ZERO
}
