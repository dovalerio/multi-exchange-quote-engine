package br.com.dovalerio.model

data class ExchangeMetrics(
    val exchange: String,
    val successCount: Int,
    val failureCount: Int,
    val breakerState: String
)
