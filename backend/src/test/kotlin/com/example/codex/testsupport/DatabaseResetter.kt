package com.example.codex.testsupport

import java.io.File
import java.sql.DriverManager

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

                val sqlScript =
                    listOf("TRUNCATE TABLE ${tables.joinToString(", ")} RESTART IDENTITY CASCADE;")
                        .plus(
                            File("build/init.sql")
                                .readLines()
                                .filter { it.trim().startsWith("INSERT", ignoreCase = true) },
                        ).joinToString("\n")

                if (sqlScript.isNotBlank()) {
                    stmt.execute(sqlScript)
                }
            }
        }
    }
}
