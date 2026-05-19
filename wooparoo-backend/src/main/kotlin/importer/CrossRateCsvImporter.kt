package org.example.importer

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.FileReader
import java.math.BigDecimal

@Component
class CrossRateCsvImporter(
    private val jdbcTemplate: JdbcTemplate,
    private val wooparooIdCache: WooparooIdCache
) {

    private val BATCH_SIZE = 2_000

    fun importCsv(
        csvPath: String,
        crossType: Int,
        luckUpEvent: Boolean
    ) {
        val sql = """
            INSERT INTO cross_rate (
                cross_type, left_id, right_id, result_id, rate, luck_up_event
            ) VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val batch = ArrayList<Array<Any>>(BATCH_SIZE)
        var count = 0

        BufferedReader(FileReader(csvPath)).use { br ->
            br.readLine()

            br.lineSequence().forEach { line ->
                val c = line.split(",")

                batch += arrayOf(
                    crossType,
                    wooparooIdCache.getId(c[1]),
                    wooparooIdCache.getId(c[2]),
                    wooparooIdCache.getId(c[0]),
                    BigDecimal(c[3]),
                    luckUpEvent
                )

                if (batch.size == BATCH_SIZE) {
                    insertBatch(sql, batch)
                    batch.clear()
                }

                if (++count % 100_000 == 0) {
                    println("Imported $count rows")
                }
            }
        }

        if (batch.isNotEmpty()) {
            insertBatch(sql, batch)
        }

        println("✅ CSV IMPORT FINISHED ($count rows)")
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun insertBatch(sql: String, batch: List<Array<Any>>) {
        jdbcTemplate.batchUpdate(sql, batch)
    }
}
