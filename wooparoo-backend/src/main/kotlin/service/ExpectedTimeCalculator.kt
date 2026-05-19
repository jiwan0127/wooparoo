package org.example.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExpectedTimeCalculator(
    private val jdbcTemplate: JdbcTemplate
) {

    fun recalculate() {
        println("🔄 Rebuilding cross_expected_time")

        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_expected_time_lr")

        jdbcTemplate.execute("TRUNCATE TABLE cross_expected_time")

        jdbcTemplate.execute("""
            INSERT INTO cross_expected_time (left_id, right_id, expected_time)
            SELECT
                cr.left_id,
                cr.right_id,
                SUM(cr.rate * w.summon_time)
            FROM cross_rate cr
            JOIN wooparoo w ON cr.result_id = w.id
            GROUP BY cr.left_id, cr.right_id
        """.trimIndent())

        jdbcTemplate.execute("CREATE INDEX idx_expected_time_lr ON cross_expected_time (left_id, right_id)".trimIndent())

        println("✅ expected_time rebuild finished")
    }
}
