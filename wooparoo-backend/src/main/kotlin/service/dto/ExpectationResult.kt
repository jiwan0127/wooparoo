package org.example.service.dto

data class ExpectationResult(
    val left: String,
    val right: String,
    val targetRate: Double,
    val expectedGem: Double
)
