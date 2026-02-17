package br.com.dovalerio.client

import br.com.dovalerio.model.ExchangeMetrics
import br.com.dovalerio.service.CircuitBreaker
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.time.Duration.Companion.seconds
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

abstract class BaseExchangeClient(
    protected val exchangeName: String,
    maxConcurrentCalls: Int = 3
) : ExchangeClient {

    private var successCount = 0
    private var failureCount = 0

    private val semaphore = Semaphore(maxConcurrentCalls)

    private val circuitBreaker = CircuitBreaker(
        failureThreshold = 3,
        openDuration = 15.seconds,
        halfOpenMaxCalls = 1
    )

    protected val httpClient = OkHttpClient.Builder()
        .connectTimeout(2.seconds.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .readTimeout(3.seconds.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .callTimeout(4.seconds.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .build()

    protected val json = Json { ignoreUnknownKeys = true }

    protected fun executeGet(url: String): String {

        return try {

            val result = circuitBreaker.execute {
                semaphore.withPermit {
                    httpClient.newCall(
                        Request.Builder()
                            .url(url)
                            .get()
                            .build()
                    ).execute().use { response ->

                        val body = response.body?.string()
                            ?: error("Empty response from $url")

                        check(response.isSuccessful) {
                            "HTTP ${response.code}: $body"
                        }

                        body
                    }
                }
            }

            successCount++
            result

        } catch (ex: Exception) {
            failureCount++
            throw ex
        }
    }

    override fun metrics(): ExchangeMetrics =
        ExchangeMetrics(
            exchange = exchangeName,
            successCount = successCount,
            failureCount = failureCount,
            breakerState = circuitBreaker.currentState()
        )

    private inline fun <T> Semaphore.withPermit(block: () -> T): T {
        acquire()
        return try {
            block()
        } finally {
            release()
        }
    }
}
