package org.example.controller

import org.example.dto.CrossRateRequest
import org.example.service.CrossRateService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cross")
class CrossRateController(
    private val crossRateService: CrossRateService
) {

    @PostMapping
    fun cross(@ModelAttribute request: CrossRateRequest) =
        crossRateService.query(request)
}
