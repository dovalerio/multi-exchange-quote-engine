package br.com.dovalerio.model

data class CurrencyPair(
    val base: CryptoAsset,
    val quote: String
) {
    override fun toString(): String = "${base.name}-$quote"
}
