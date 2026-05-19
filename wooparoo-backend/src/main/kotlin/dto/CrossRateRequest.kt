package org.example.dto

data class CrossRateRequest(
    val left: String,
    val right: String,
    val crossType: Int,
    val luckUpEvent: Boolean = false
)
