package org.example.controller

import org.example.importer.WooparooIdCache
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CrossExpectationController(
    private val wooparooIdCache: WooparooIdCache
) {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("wooparoos", wooparooIdCache.allNames())
        return "expectation"
    }
}
