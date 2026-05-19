package org.example.controller

import org.example.service.CrossImportService
import org.example.service.ExpectedTimeCalculator
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminImportController(
    private val crossImportService: CrossImportService,
    private val expectedTimeCalculator: ExpectedTimeCalculator
) {

    @PostMapping("/import/cross-rate")
    fun importCrossRate(
        @RequestParam path: String,
        @RequestParam crossType: Int,
        @RequestParam(defaultValue = "false") luckUpEvent: Boolean
    ): String {
        crossImportService.importAndRebuild(path, crossType, luckUpEvent)
        return "cross_rate import OK"
    }

    @PostMapping("/calc/expected-time")
    fun calculateExpectedTime(): String {
        expectedTimeCalculator.recalculate()
        return "expected time recalculated"
    }
}
