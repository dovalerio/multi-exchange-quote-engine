package br.com.dovalerio.model

import java.math.BigDecimal

data class Quote(
    val exchange: String,
    val pair: String,
    val bid: BigDecimal,
    val ask: BigDecimal,
    val timestamp: Long
)
