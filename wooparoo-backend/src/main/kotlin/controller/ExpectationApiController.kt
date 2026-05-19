package org.example.controller

import org.example.service.CrossExpectationService
import org.example.service.dto.ExpectationResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ExpectationApiController(
    private val crossExpectationService: CrossExpectationService
) {

    @PostMapping("/expectation")
    fun expectation(
        @RequestParam target: String,
        @RequestParam(defaultValue = "2") crossType: Int
    ): List<ExpectationResult> {
        return crossExpectationService.findBestCombinations(target, crossType)
    }

    @PostMapping("/expectation/multi")
    fun expectationMulti(
        @RequestParam target: List<String>,
        @RequestParam(required = false) exclude: List<String>?,
        @RequestParam(defaultValue = "2") crossType: Int
    ): List<ExpectationResult> {
        return crossExpectationService.findBestCombinationsMulti(target, exclude ?: emptyList(), crossType)
    }
}
