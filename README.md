# multi-exchange-quote-engine
# multi-exchange-quote-engine

Console application written in Kotlin running on Java 25 to aggregate cryptocurrency prices from multiple exchanges using Virtual Threads (Project Loom).

The goal of this project is to demonstrate high concurrency with a simple and readable imperative model, without reactive frameworks.

---

## ✨ Features

- Parallel calls to multiple exchanges
- Virtual thread per request
- Price normalization
- Best bid / ask calculation
- Execution time measurement
- Easy evolution to REST or event-driven architecture

---

## 🧠 Architecture Overview

```

Main
→ QuoteAggregator
→ ExchangeClient (interface)
→ BinanceClient
→ MercadoBitcoinClient

````

Each provider implements the same contract, enabling extension without modifying the aggregator.

---

## 🚀 Tech Stack

- Kotlin JVM
- Java 25
- Virtual Threads (Project Loom)
- OkHttp
- kotlinx.serialization
- Logback

---

## ▶ Running the project

### Requirements

- JDK 25
- Gradle

### Command

```bash
./gradlew run
````

---

## 🧪 Example Output

```
BTC/BRL
---------------------------------
Binance         bid=301800 ask=302200
MercadoBitcoin  bid=302000 ask=302500

Best BUY : Binance
Best SELL: MercadoBitcoin

Completed in 143 ms
```

---

## 📈 Why Virtual Threads?

Crypto exchanges are I/O bound systems.
Virtual threads allow thousands of concurrent operations with minimal resource consumption while keeping code simple and synchronous.

---

## 🔮 Future Improvements

* REST API
* WebSocket streaming
* Distributed cache
* Kafka event publishing
* Historical persistence
* Observability (metrics + tracing)
* Load testing dashboards

---

## 📄 License

MIT

````



