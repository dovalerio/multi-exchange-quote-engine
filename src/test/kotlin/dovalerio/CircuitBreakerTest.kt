package dovalerio

import br.com.dovalerio.service.CircuitBreaker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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
}
