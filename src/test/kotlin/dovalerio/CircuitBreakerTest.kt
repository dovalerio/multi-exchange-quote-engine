package dovalerio

import br.com.dovalerio.service.CircuitBreaker
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CircuitBreakerTest {

    @Test
    fun `should open circuit after threshold`() {

        val breaker = CircuitBreaker(failureThreshold = 2)

        assertThrows(Exception::class.java) {
            breaker.execute { error("fail") }
        }

        assertThrows(Exception::class.java) {
            breaker.execute { error("fail") }
        }

        assertEquals("OPEN", breaker.currentState())
    }

    @Test
    fun `should start in CLOSED state`() {

        val breaker = CircuitBreaker()

        assertEquals("CLOSED", breaker.currentState())
    }

    @Test
    fun `should execute successfully in CLOSED state`() {

        val breaker = CircuitBreaker()

        val result = breaker.execute { 42 }

        assertEquals(42, result)
        assertEquals("CLOSED", breaker.currentState())
    }

    @Test
    fun `should remain CLOSED when failures are below threshold`() {

        val breaker = CircuitBreaker(failureThreshold = 3)

        assertThrows(Exception::class.java) { breaker.execute { error("fail") } }
        assertThrows(Exception::class.java) { breaker.execute { error("fail") } }

        assertEquals("CLOSED", breaker.currentState())
    }

    @Test
    fun `should reset failure count after a success`() {

        val breaker = CircuitBreaker(failureThreshold = 3)

        repeat(2) { assertThrows(Exception::class.java) { breaker.execute { error("fail") } } }

        breaker.execute { "ok" }

        // 2 more failures after reset should not open (threshold not reached again)
        repeat(2) { assertThrows(Exception::class.java) { breaker.execute { error("fail") } } }

        assertEquals("CLOSED", breaker.currentState())
    }

    @Test
    fun `should throw with OPEN message when cooldown has not elapsed`() {

        val breaker = CircuitBreaker(failureThreshold = 1, openDuration = 60.seconds)

        assertThrows(Exception::class.java) { breaker.execute { error("fail") } }

        val ex = assertThrows(IllegalStateException::class.java) {
            breaker.execute { "should not run" }
        }

        assertTrue(ex.message!!.contains("OPEN"))
    }

    @Test
    fun `should recover to CLOSED after successful HALF_OPEN call`() {

        val breaker = CircuitBreaker(failureThreshold = 1, openDuration = 1.milliseconds)

        assertThrows(Exception::class.java) { breaker.execute { error("fail") } }
        assertEquals("OPEN", breaker.currentState())

        Thread.sleep(20)

        val result = breaker.execute { "recovered" }

        assertEquals("recovered", result)
        assertEquals("CLOSED", breaker.currentState())
    }

    @Test
    fun `should trip back to OPEN after failed HALF_OPEN call`() {

        val breaker = CircuitBreaker(failureThreshold = 1, openDuration = 1.milliseconds)

        assertThrows(Exception::class.java) { breaker.execute { error("fail") } }

        Thread.sleep(20)

        assertThrows(Exception::class.java) { breaker.execute { error("still failing") } }

        assertEquals("OPEN", breaker.currentState())
    }
}
