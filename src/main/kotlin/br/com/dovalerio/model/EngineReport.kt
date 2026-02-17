package br.com.dovalerio.model

data class EngineReport(
    val spreads: List<SpreadResult>,
    val metrics: List<ExchangeMetrics>
)
