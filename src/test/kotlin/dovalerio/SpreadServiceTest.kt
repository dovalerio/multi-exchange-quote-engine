package dovalerio

import br.com.dovalerio.model.CryptoAsset
import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.Quote
import br.com.dovalerio.service.SpreadService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SpreadServiceTest {

    private val service = SpreadService()

    @Test
    fun `should calculate correct spread`() {

        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")

        val quotes = listOf(
            Quote("Binance", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0),
            Quote("MercadoBitcoin", pair.toString(), BigDecimal("110"), BigDecimal("115"), 0)
        )

        val result = service.calculate(pair, quotes)

        assertNotNull(result)
        assertEquals("5.00", result!!.spread.toPlainString())
        assertEquals("Binance", result.bestBuy.exchange)
        assertEquals("MercadoBitcoin", result.bestSell.exchange)
    }

    @Test
    fun `should return null when insufficient quotes`() {

        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")

        val quotes = listOf(
            Quote("Binance", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0)
        )

        val result = service.calculate(pair, quotes)

        assertNull(result)
    }

    @Test
    fun `should return null when quotes list is empty`() {

        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")

        val result = service.calculate(pair, emptyList())

        assertNull(result)
    }

    @Test
    fun `should identify best buy and best sell among three quotes`() {

        val pair = CurrencyPair(CryptoAsset.ETH, "BRL")

        // ask:  A=120, B=130, C=108  → bestBuy  = C (lowest ask)
        // bid:  A=100, B=120, C=110 → bestSell = B (highest bid)
        val quotes = listOf(
            Quote("A", pair.toString(), BigDecimal("100"), BigDecimal("120"), 0),
            Quote("B", pair.toString(), BigDecimal("120"), BigDecimal("130"), 0),
            Quote("C", pair.toString(), BigDecimal("110"), BigDecimal("108"), 0)
        )

        val result = service.calculate(pair, quotes)

        assertNotNull(result)
        assertEquals("C", result!!.bestBuy.exchange)   // lowest ask = 108
        assertEquals("B", result.bestSell.exchange)    // highest bid = 120
        assertEquals("12.00", result.spread.toPlainString()) // 120 - 108
    }

    @Test
    fun `should calculate negative spread when no arbitrage opportunity exists`() {

        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")

        // bestBuy = A (ask=105), bestSell = A (bid=100) → spread = 100 - 105 = -5.00
        val quotes = listOf(
            Quote("A", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0),
            Quote("B", pair.toString(), BigDecimal("95"),  BigDecimal("110"), 0)
        )

        val result = service.calculate(pair, quotes)

        assertNotNull(result)
        assertEquals("-5.00", result!!.spread.toPlainString())
        assertFalse(result.hasArbitrage)
    }
}
