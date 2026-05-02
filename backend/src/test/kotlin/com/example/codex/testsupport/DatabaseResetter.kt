package com.example.codex.testsupport

import io.github.oshai.kotlinlogging.KotlinLogging
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import java.io.File
import java.io.PrintWriter
import java.sql.DriverManager
import java.util.*
import kotlin.use

class DatabaseResetter(
    private val jdbcUrl: String,
    private val username: String,
    private val password: String,
) {

    fun resetDatabase() {
        DriverManager.getConnection(jdbcUrl, username, password).use { conn ->
            conn.createStatement().use { stmt ->
                val tables = mutableListOf<String>()
                conn.metaData.getTables(null, null, "%", arrayOf("TABLE")).use { rs ->
                    while (rs.next()) tables.add(rs.getString("TABLE_NAME"))
                }

                val sqlScript = listOf("TRUNCATE TABLE ${tables.joinToString(", ")} RESTART IDENTITY CASCADE;")
                    .plus(
                        File("build/init.sql")
                            .readLines()
                            .filter { it.trim().startsWith("INSERT", ignoreCase = true) }
                    ).joinToString("\n")

                if (sqlScript.isNotBlank()) {
                    stmt.execute(sqlScript)
                }
            }
        }
    }
}
