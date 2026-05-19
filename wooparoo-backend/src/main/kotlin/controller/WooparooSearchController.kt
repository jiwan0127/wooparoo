package org.example.controller

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WooparooSearchController(
    private val jdbcTemplate: JdbcTemplate
) {

    @GetMapping("/api/wooparoo/search")
    fun search(
        @RequestParam keyword: String
    ): List<String> {

        if (keyword.isBlank()) return emptyList()

        return jdbcTemplate.queryForList(
            """
        SELECT name
        FROM wooparoo
        WHERE name LIKE ?
        ORDER BY id
        LIMIT 20
        """.trimIndent(),
            String::class.java,
            "%$keyword%"
        )
    }

}
