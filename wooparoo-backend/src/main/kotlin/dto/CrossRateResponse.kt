package org.example.dto

import java.math.BigDecimal

data class CrossRateResponse(
    val wooparoo: String,
    val rate: BigDecimal
)
