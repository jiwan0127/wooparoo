package org.example.repository

import org.example.dto.CrossRateResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class CrossRateRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun findTop50(
        leftId: Long,
        rightId: Long,
        crossType: Int,
        luckUpEvent: Boolean
    ): List<CrossRateResponse> {

        val sql = """
            SELECT 
                w.name AS wooparoo,
                cr.rate
            FROM cross_rate cr
            JOIN wooparoo w ON cr.result_id = w.id
            WHERE cr.cross_type = ?
              AND cr.luck_up_event = ?
              AND cr.left_id = ?
              AND cr.right_id = ?
            ORDER BY cr.rate DESC
            LIMIT 50
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            { rs, _ ->
                CrossRateResponse(
                    wooparoo = rs.getString("wooparoo"),
                    rate = rs.getBigDecimal("rate")
                )
            },
            crossType,
            luckUpEvent,
            leftId,
            rightId
        )
    }
}
