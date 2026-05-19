package org.example.service

import org.example.importer.CrossRateCsvImporter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class CrossImportService(
    private val crossRateCsvImporter: CrossRateCsvImporter,
    private val jdbcTemplate: JdbcTemplate
) {

    fun importAndRebuild(
        csvPath: String,
        crossType: Int,
        luckUpEvent: Boolean
    ) {
        println("🔧 Drop indexes")
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_cross_lookup")
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_cross_expect")
        jdbcTemplate.execute("DROP INDEX IF EXISTS idx_cross_rate_result_pair")

        println("🧹 Truncate table")
        jdbcTemplate.execute("TRUNCATE TABLE cross_rate")

        crossRateCsvImporter.importCsv(csvPath, crossType, luckUpEvent)

        println("📌 Create indexes")
        jdbcTemplate.execute("CREATE INDEX idx_cross_lookup ON cross_rate (cross_type, luck_up_event, left_id, right_id)".trimIndent())
        jdbcTemplate.execute("CREATE INDEX idx_cross_expect ON cross_rate (result_id, cross_type, left_id, right_id)".trimIndent())
        jdbcTemplate.execute("CREATE INDEX idx_cross_rate_result_pair ON cross_rate (result_id, left_id, right_id)".trimIndent())
    }
}
