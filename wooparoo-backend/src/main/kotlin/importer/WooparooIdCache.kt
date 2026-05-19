package org.example.importer

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.text.Normalizer
import java.util.concurrent.ConcurrentHashMap

@Component
class WooparooIdCache(
    private val jdbcTemplate: JdbcTemplate
) {
    private val nameToId = ConcurrentHashMap<String, Long>()
    private val idToName = ConcurrentHashMap<Long, String>()
    @Volatile private var initialized = false

    private fun normalize(name: String): String =
        Normalizer.normalize(name.trim(), Normalizer.Form.NFC)

    private fun initIfNeeded() {
        if (initialized) return

        synchronized(this) {
            if (initialized) return

            jdbcTemplate.query(
                "SELECT id, name FROM wooparoo"
            ) { rs ->
                val id = rs.getLong("id")
                val name = normalize(rs.getString("name"))
                nameToId[name] = id
                idToName[id] = name
            }

            initialized = true
        }
    }

    fun getId(name: String): Long {
        initIfNeeded()
        return nameToId[normalize(name)]
            ?: error("Unknown wooparoo name: [$name]")
    }

    fun getName(id: Long): String {
        initIfNeeded()
        return idToName[id]
            ?: error("Unknown wooparoo id: $id")
    }

    fun allNames(): List<String> {
        initIfNeeded()
        return nameToId.keys.sorted()
    }
}
