package br.com.dovalerio.service

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class CircuitBreaker(

private val failureThreshold: Int = 3,
    private val openDuration: Duration = 15.seconds,
    private val halfOpenMaxCalls: Int = 1
) {

    private enum class State { CLOSED, OPEN, HALF_OPEN }

    @Volatile
    private var state: State = State.CLOSED

    private var failures: Int = 0
    private var halfOpenCalls: Int = 0

    private var openedAt = TimeSource.Monotonic.markNow()
    fun currentState(): String = state.name
    @Synchronized
    fun <T> execute(block: () -> T): T {

        return when (state) {

            State.CLOSED -> runClosed(block)

            State.OPEN -> {
                if (openedAt.elapsedNow() >= openDuration) {
                    state = State.HALF_OPEN
                    halfOpenCalls = 0
                    runHalfOpen(block)
                } else {
                    error("Circuit breaker OPEN (cooldown active)")
                }
            }

            State.HALF_OPEN -> runHalfOpen(block)
        }
    }

    private fun <T> runClosed(block: () -> T): T {
        return runCatching { block() }
            .onSuccess {
                failures = 0
            }
            .onFailure {
                failures++
                if (failures >= failureThreshold) {
                    tripOpen()
                }
            }
            .getOrThrow()
    }

    private fun <T> runHalfOpen(block: () -> T): T {

        if (++halfOpenCalls > halfOpenMaxCalls) {
            error("Circuit breaker HALF_OPEN (test calls exceeded)")
        }

        return runCatching { block() }
            .onSuccess {
                resetClosed()
            }
            .onFailure {
                tripOpen()
            }
            .getOrThrow()
    }

    private fun tripOpen() {
        state = State.OPEN
        openedAt = TimeSource.Monotonic.markNow()
        println("⚡ Circuit breaker OPENED")
    }

    private fun resetClosed() {
        state = State.CLOSED
        failures = 0
        halfOpenCalls = 0
        println("✅ Circuit breaker CLOSED")
    }
}
