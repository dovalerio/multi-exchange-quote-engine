package br.com.dovalerio

import br.com.dovalerio.model.CryptoPriceEvent
import br.com.dovalerio.model.CurrencyPair
import br.com.dovalerio.model.SpreadEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

class KafkaCryptoProducer(
    bootstrapServers: String
) : AutoCloseable {

    private val objectMapper = jacksonObjectMapper()

    private val producer: KafkaProducer<String, String>

    init {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.RETRIES_CONFIG, 3)
            put(ProducerConfig.LINGER_MS_CONFIG, 5)
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
        }

        producer = KafkaProducer(props)
    }

    fun sendCryptoPrice(event: CryptoPriceEvent) {
        send("crypto.prices", event.symbol, event)
    }

    fun sendSpread(event: SpreadEvent) {
        send("crypto.spreads", event.pair, event)
    }

    private fun send(topic: String, key: CurrencyPair, payload: Any) {
        val json = objectMapper.writeValueAsString(payload)

        val record = ProducerRecord<String, String>(topic, key.toString(), json)

        producer.send(record) { metadata, exception ->
            if (exception != null) {
                println("Erro ao enviar evento para $topic: ${exception.message}")
            } else {
                println("Publicado em ${metadata.topic()} offset ${metadata.offset()}")
            }
        }
    }

    override fun close() {
        producer.flush()
        producer.close()
    }
}