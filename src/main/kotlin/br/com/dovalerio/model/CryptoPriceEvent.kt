package br.com.dovalerio.model

import java.math.BigDecimal

data class CryptoPriceEvent(
    val symbol: CurrencyPair,
    val exchange: String,
    val price: BigDecimal,
    val timestamp: Long
)