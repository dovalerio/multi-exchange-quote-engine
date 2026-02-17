package br.com.dovalerio.service

import br.com.dovalerio.model.SpreadResult

class ArbitrageDetector {

    fun detect(spreadResult: SpreadResult): Boolean {
        return spreadResult.hasArbitrage
    }
}
