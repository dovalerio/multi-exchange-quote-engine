package dovalerio

import br.com.dovalerio.aggregator.QuoteAggregator
import br.com.dovalerio.client.ExchangeClient
import br.com.dovalerio.model.CryptoAsset
import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.Quote
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class QuoteAggregatorTest {

    @Test
    fun `should return quotes for all crypto assets from a single client`() {

        val client = mockk<ExchangeClient>()
        val aggregator = QuoteAggregator(listOf(client))

        CryptoAsset.entries.forEach { asset ->
            val pair = CurrencyPair(asset, "BRL")
            every { client.getQuote(pair) } returns
                Quote("Test", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0)
        }

        val result = aggregator.getAllQuotes("BRL")

        assertTrue(result.isSuccess)
        assertEquals(CryptoAsset.entries.size, result.getOrThrow().size)
    }

    @Test
    fun `should return quotes from multiple clients`() {

        val client1 = mockk<ExchangeClient>()
        val client2 = mockk<ExchangeClient>()
        val aggregator = QuoteAggregator(listOf(client1, client2))

        CryptoAsset.entries.forEach { asset ->
            val pair = CurrencyPair(asset, "BRL")
            every { client1.getQuote(pair) } returns Quote("C1", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0)
            every { client2.getQuote(pair) } returns Quote("C2", pair.toString(), BigDecimal("110"), BigDecimal("115"), 0)
        }

        val result = aggregator.getAllQuotes("BRL")

        assertTrue(result.isSuccess)
        // 5 assets × 2 clients = 10 quotes
        assertEquals(CryptoAsset.entries.size * 2, result.getOrThrow().size)
    }

    @Test
    fun `should skip individual quotes that fail and return the rest`() {

        val client = mockk<ExchangeClient>()
        val aggregator = QuoteAggregator(listOf(client))

        every { client.getQuote(CurrencyPair(CryptoAsset.BTC, "BRL")) } returns
            Quote("Test", "BTC-BRL", BigDecimal("100"), BigDecimal("105"), 0)

        CryptoAsset.entries.filter { it != CryptoAsset.BTC }.forEach { asset ->
            every { client.getQuote(CurrencyPair(asset, "BRL")) } throws RuntimeException("timeout")
        }

        val result = aggregator.getAllQuotes("BRL")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("BTC-BRL", result.getOrThrow().first().pair)
    }

    @Test
    fun `should return empty list when all client calls fail`() {

        val client = mockk<ExchangeClient>()
        val aggregator = QuoteAggregator(listOf(client))

        CryptoAsset.entries.forEach { asset ->
            every { client.getQuote(CurrencyPair(asset, "BRL")) } throws RuntimeException("connection refused")
        }

        val result = aggregator.getAllQuotes("BRL")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }
}
