package dovalerio

import br.com.dovalerio.model.CryptoAsset
import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.ExchangeMetrics
import br.com.dovalerio.model.Quote
import br.com.dovalerio.service.ArbitrageDetector
import br.com.dovalerio.service.EngineService
import br.com.dovalerio.service.QuoteService
import br.com.dovalerio.service.SpreadService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class EngineServiceTest {

    private val quoteService = mockk<QuoteService>(relaxed = true)
    private val spreadService = SpreadService()
    private val arbitrageDetector = ArbitrageDetector()

    private val engineService = EngineService(
        quoteService,
        spreadService,
        arbitrageDetector
    )

    @Test
    fun `should generate engine report`() {

        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")

        val quotes = listOf(
            Quote("A", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0),
            Quote("B", pair.toString(), BigDecimal("110"), BigDecimal("115"), 0)
        )

        every { quoteService.fetchAll("BRL") } returns Result.success(quotes)
        every { quoteService.collectMetrics() } returns emptyList()

        val result = engineService.execute("BRL")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().spreads.size)
    }

    @Test
    fun `should return failure when quote service returns failure`() {

        every { quoteService.fetchAll("BRL") } returns Result.failure(RuntimeException("network error"))

        val result = engineService.execute("BRL")

        assertTrue(result.isFailure)
    }

    @Test
    fun `should return empty spreads when no quotes are available`() {

        every { quoteService.fetchAll("BRL") } returns Result.success(emptyList())
        every { quoteService.collectMetrics() } returns emptyList()

        val result = engineService.execute("BRL")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().spreads.isEmpty())
    }

    @Test
    fun `should return empty spreads when each asset has only one quote`() {

        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")

        val quotes = listOf(
            Quote("A", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0)
        )

        every { quoteService.fetchAll("BRL") } returns Result.success(quotes)
        every { quoteService.collectMetrics() } returns emptyList()

        val result = engineService.execute("BRL")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().spreads.isEmpty())
    }

    @Test
    fun `should include metrics from quote service in report`() {

        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")

        val quotes = listOf(
            Quote("A", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0),
            Quote("B", pair.toString(), BigDecimal("110"), BigDecimal("115"), 0)
        )

        val metrics = listOf(
            ExchangeMetrics("Binance", 10, 0, "CLOSED"),
            ExchangeMetrics("MercadoBitcoin", 8, 1, "CLOSED")
        )

        every { quoteService.fetchAll("BRL") } returns Result.success(quotes)
        every { quoteService.collectMetrics() } returns metrics

        val result = engineService.execute("BRL")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().metrics.size)
        assertEquals("Binance", result.getOrThrow().metrics[0].exchange)
    }

    @Test
    fun `should produce spreads for multiple assets`() {

        val btcPair = CurrencyPair(CryptoAsset.BTC, "BRL")
        val ethPair = CurrencyPair(CryptoAsset.ETH, "BRL")

        val quotes = listOf(
            Quote("A", btcPair.toString(), BigDecimal("100"), BigDecimal("105"), 0),
            Quote("B", btcPair.toString(), BigDecimal("110"), BigDecimal("115"), 0),
            Quote("A", ethPair.toString(), BigDecimal("10"),  BigDecimal("11"),  0),
            Quote("B", ethPair.toString(), BigDecimal("12"),  BigDecimal("13"),  0)
        )

        every { quoteService.fetchAll("BRL") } returns Result.success(quotes)
        every { quoteService.collectMetrics() } returns emptyList()

        val result = engineService.execute("BRL")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().spreads.size)
    }
}
