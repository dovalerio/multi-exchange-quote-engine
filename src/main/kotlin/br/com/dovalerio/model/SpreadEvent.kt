package br.com.dovalerio.model

import java.math.BigDecimal

data class SpreadEvent(
    val pair: CurrencyPair,
    val bestBuyExchange: String,
    val bestBuyPrice: BigDecimal,
    val bestSellExchange: String,
    val bestSellPrice: BigDecimal,
    val spread: BigDecimal,
    val hasArbitrage: Boolean,
    val timestamp: Long
)