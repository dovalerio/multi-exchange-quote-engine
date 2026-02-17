package dovalerio

import br.com.dovalerio.model.CryptoAsset
import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.Quote
import br.com.dovalerio.service.ArbitrageDetector
import br.com.dovalerio.service.EngineService
import br.com.dovalerio.service.QuoteService
import br.com.dovalerio.service.SpreadService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
}
