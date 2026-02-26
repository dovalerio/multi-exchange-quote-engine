package dovalerio

import br.com.dovalerio.aggregator.QuoteAggregator
import br.com.dovalerio.client.ExchangeClient
import br.com.dovalerio.model.ExchangeMetrics
import br.com.dovalerio.model.Quote
import br.com.dovalerio.service.QuoteService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class QuoteServiceTest {

    private val aggregator = mockk<QuoteAggregator>()
    private val client1 = mockk<ExchangeClient>()
    private val client2 = mockk<ExchangeClient>()
    private val service = QuoteService(aggregator, listOf(client1, client2))

    @Test
    fun `should delegate fetchAll to aggregator`() {

        val quotes = listOf(Quote("A", "BTC-BRL", BigDecimal("100"), BigDecimal("105"), 0))
        every { aggregator.getAllQuotes("BRL") } returns Result.success(quotes)

        val result = service.fetchAll("BRL")

        assertTrue(result.isSuccess)
        assertEquals(quotes, result.getOrThrow())
    }

    @Test
    fun `should propagate failure from aggregator`() {

        every { aggregator.getAllQuotes("BRL") } returns Result.failure(RuntimeException("timeout"))

        val result = service.fetchAll("BRL")

        assertTrue(result.isFailure)
    }

    @Test
    fun `should collect metrics from all clients`() {

        val metrics1 = ExchangeMetrics("Binance", 10, 0, "CLOSED")
        val metrics2 = ExchangeMetrics("MercadoBitcoin", 5, 1, "CLOSED")
        every { client1.metrics() } returns metrics1
        every { client2.metrics() } returns metrics2

        val result = service.collectMetrics()

        assertEquals(2, result.size)
        assertTrue(result.containsAll(listOf(metrics1, metrics2)))
    }
}
