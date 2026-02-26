package dovalerio

import br.com.dovalerio.model.CryptoAsset
import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.EngineReport
import br.com.dovalerio.model.ExchangeMetrics
import br.com.dovalerio.model.Quote
import br.com.dovalerio.model.SpreadResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ModelTest {

    // ── CurrencyPair ──────────────────────────────────────────────────────────

    @Test
    fun `CurrencyPair toString should return BASE-QUOTE format`() {
        assertEquals("BTC-BRL", CurrencyPair(CryptoAsset.BTC, "BRL").toString())
        assertEquals("ETH-USD", CurrencyPair(CryptoAsset.ETH, "USD").toString())
        assertEquals("SOL-BRL", CurrencyPair(CryptoAsset.SOL, "BRL").toString())
    }

    @Test
    fun `CurrencyPair data class equality should compare by value`() {
        val p1 = CurrencyPair(CryptoAsset.BTC, "BRL")
        val p2 = CurrencyPair(CryptoAsset.BTC, "BRL")
        val p3 = CurrencyPair(CryptoAsset.ETH, "BRL")

        assertEquals(p1, p2)
        assertNotSame(p1, p2)
        assertNotEquals(p1, p3)
    }

    // ── SpreadResult ──────────────────────────────────────────────────────────

    @Test
    fun `SpreadResult hasArbitrage should return true for positive spread`() {
        assertTrue(makeSpreadResult("10.00").hasArbitrage)
    }

    @Test
    fun `SpreadResult hasArbitrage should return false for zero spread`() {
        assertFalse(makeSpreadResult("0.00").hasArbitrage)
    }

    @Test
    fun `SpreadResult hasArbitrage should return false for negative spread`() {
        assertFalse(makeSpreadResult("-3.50").hasArbitrage)
    }

    @Test
    fun `SpreadResult data class equality should compare by value`() {
        val r1 = makeSpreadResult("5.00")
        val r2 = makeSpreadResult("5.00")
        assertEquals(r1, r2)
    }

    // ── Quote ─────────────────────────────────────────────────────────────────

    @Test
    fun `Quote data class equality should compare by value`() {
        val q1 = Quote("Binance", "BTC-BRL", BigDecimal("100"), BigDecimal("105"), 1000L)
        val q2 = Quote("Binance", "BTC-BRL", BigDecimal("100"), BigDecimal("105"), 1000L)
        assertEquals(q1, q2)
    }

    // ── ExchangeMetrics ───────────────────────────────────────────────────────

    @Test
    fun `ExchangeMetrics data class equality should compare by value`() {
        val m1 = ExchangeMetrics("Binance", 10, 2, "CLOSED")
        val m2 = ExchangeMetrics("Binance", 10, 2, "CLOSED")
        assertEquals(m1, m2)
    }

    // ── EngineReport ──────────────────────────────────────────────────────────

    @Test
    fun `EngineReport should expose spreads and metrics`() {
        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")
        val spread = makeSpreadResult("5.00")
        val metric = ExchangeMetrics("Binance", 1, 0, "CLOSED")

        val report = EngineReport(spreads = listOf(spread), metrics = listOf(metric))

        assertEquals(1, report.spreads.size)
        assertEquals(1, report.metrics.size)
        assertEquals("Binance", report.metrics[0].exchange)
    }

    // ── CryptoAsset ───────────────────────────────────────────────────────────

    @Test
    fun `CryptoAsset should contain all expected assets`() {
        val names = CryptoAsset.entries.map { it.name }
        assertTrue(names.containsAll(listOf("BTC", "ETH", "SOL", "XRP", "ADA")))
        assertEquals(5, CryptoAsset.entries.size)
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeSpreadResult(spread: String): SpreadResult {
        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")
        return SpreadResult(
            pair = pair,
            bestBuy  = Quote("A", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0),
            bestSell = Quote("B", pair.toString(), BigDecimal("110"), BigDecimal("115"), 0),
            spread   = BigDecimal(spread)
        )
    }
}
