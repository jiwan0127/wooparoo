package org.example.service

import org.example.importer.WooparooIdCache
import org.example.service.dto.ExpectationResult
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class CrossExpectationService(
    private val jdbcTemplate: JdbcTemplate,
    private val wooparooIdCache: WooparooIdCache
) {

    fun findBestCombinations(
        targetName: String,
        crossType: Int
    ): List<ExpectationResult> {

        val targetId = wooparooIdCache.getId(targetName)

        val sql = """
            SELECT
                cr.left_id,
                cr.right_id,
                cr.rate AS target_rate,
                et.expected_time,
                (et.expected_time / cr.rate) AS expected_seconds
            FROM cross_rate cr
            JOIN cross_expected_time et
              ON cr.left_id = et.left_id
             AND cr.right_id = et.right_id
            WHERE cr.result_id = ?
              AND cr.cross_type = ?
              AND cr.left_id < cr.right_id
            ORDER BY expected_seconds ASC
            LIMIT 50
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            arrayOf(targetId, crossType)
        ) { rs, _ ->

            val targetRate = rs.getDouble("target_rate")
            val expectedTime = rs.getDouble("expected_time")

            val expectedGem = (expectedTime / targetRate) / 1200.0

            ExpectationResult(
                left = wooparooIdCache.getName(rs.getLong("left_id")),
                right = wooparooIdCache.getName(rs.getLong("right_id")),
                targetRate = targetRate,
                expectedGem = expectedGem
            )
        }
            .sortedBy { it.expectedGem }
            .take(50)
    }

    fun findBestCombinationsMulti(
        targets: List<String>,
        excludes: List<String>,
        crossType: Int
    ): List<ExpectationResult> {

        if (targets.isEmpty()) return emptyList()
        if (targets.size == 1 && excludes.isEmpty()) {
            return findBestCombinations(targets.first(), crossType)
        }

        val targetIds = targets.map { wooparooIdCache.getId(it) }
        val excludeIds = excludes.map { wooparooIdCache.getId(it) }
        val targetPlaceholders = targetIds.joinToString(",") { "?" }
        val excludePlaceholders = excludeIds.joinToString(",") { "?" }

        val excludeSql = if (excludeIds.isNotEmpty()) {
            """
            AND (cr.left_id, cr.right_id) NOT IN (
                SELECT cr2.left_id, cr2.right_id
                FROM cross_rate cr2
                WHERE cr2.result_id IN ($excludePlaceholders)
            )
            """
        } else {
            ""
        }

        val sql = """
            SELECT
                cr.left_id,
                cr.right_id,
                SUM(cr.rate) AS total_rate,
                et.expected_time
            FROM cross_rate cr
            JOIN cross_expected_time et
              ON cr.left_id = et.left_id
             AND cr.right_id = et.right_id
            WHERE cr.result_id IN ($targetPlaceholders)
              AND cr.cross_type = ?
              AND cr.left_id < cr.right_id
              $excludeSql
            GROUP BY cr.left_id, cr.right_id, et.expected_time
            HAVING COUNT(DISTINCT cr.result_id) = ?
            ORDER BY (et.expected_time / SUM(cr.rate)) ASC
            LIMIT 50
        """.trimIndent()

        return jdbcTemplate.query({ conn ->
            val ps = conn.prepareStatement(sql)
            var idx = 1

            targetIds.forEach {
                ps.setLong(idx++, it)
            }

            ps.setInt(idx++, crossType)

            excludeIds.forEach {
                ps.setLong(idx++, it)
            }

            ps.setInt(idx, targetIds.size)

            ps
        }) { rs, _ ->
            val totalRate = rs.getDouble("total_rate")
            val expectedTime = rs.getDouble("expected_time")

            ExpectationResult(
                left = wooparooIdCache.getName(rs.getLong("left_id")),
                right = wooparooIdCache.getName(rs.getLong("right_id")),
                targetRate = totalRate,
                expectedGem = (expectedTime / totalRate) / 1200.0
            )
        }
    }
}
