package dovalerio

import br.com.dovalerio.model.CryptoAsset
import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.Quote
import br.com.dovalerio.service.SpreadService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
}
