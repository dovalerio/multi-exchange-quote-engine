package dovalerio

import br.com.dovalerio.model.CryptoAsset
import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.Quote
import br.com.dovalerio.model.SpreadResult
import br.com.dovalerio.service.ArbitrageDetector
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ArbitrageDetectorTest {

    private val detector = ArbitrageDetector()

    @Test
    fun `should detect arbitrage when spread positive`() {

        val spreadResult = mockSpread("10.00")

        assertTrue(detector.detect(spreadResult))
    }

    @Test
    fun `should not detect arbitrage when spread negative`() {

        val spreadResult = mockSpread("-5.00")

        assertFalse(detector.detect(spreadResult))
    }

    @Test
    fun `should not detect arbitrage when spread is zero`() {

        val spreadResult = mockSpread("0.00")

        assertFalse(detector.detect(spreadResult))
    }

    private fun mockSpread(value: String): SpreadResult {
        val pair = CurrencyPair(CryptoAsset.BTC, "BRL")

        return SpreadResult(
            pair = pair,
            bestBuy = Quote("A", pair.toString(), BigDecimal("100"), BigDecimal("105"), 0),
            bestSell = Quote("B", pair.toString(), BigDecimal("110"), BigDecimal("115"), 0),
            spread = BigDecimal(value)
        )
    }
}
