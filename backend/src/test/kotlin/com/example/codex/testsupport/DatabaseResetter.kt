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
    companion object {
        private val snapshotDir = File("build/test-snapshot")
        private val snapshotMetadataFile = File("build/test-snapshot/tables.txt")
        private val log = KotlinLogging.logger {}
    }

    fun resetDatabase() {
        // If a snapshot exists, truncate tables and restore from snapshot
        if (snapshotDir.exists() && snapshotMetadataFile.exists()) {
            try {
                restoreDataFromSnapshot()
                return
            } catch (ex: Exception) {
                log.warn(ex) { "Snapshot restore failed: ${ex.message}." }
            }
        }
    }

    private fun restoreDataFromSnapshot() {
        val props =
            Properties().apply {
                setProperty("user", username)
                setProperty("password", password)
            }

        DriverManager.getConnection(jdbcUrl, props).use { conn ->

            try {
                conn.autoCommit = false

                val baseConn = conn.unwrap(BaseConnection::class.java)
                val copyManager = CopyManager(baseConn)
                val tablesFromMetadata = snapshotMetadataFile.readLines().filter { it.isNotBlank() }
                val tablesToTruncate = getTableNames(conn)

                conn.createStatement().use { stmt ->
                    stmt.execute("SET session_replication_role = 'replica';")

                    if (tablesToTruncate.isNotEmpty()) {
                        val truncateSql = tablesToTruncate.joinToString(", ") { it }
                        val sql = "TRUNCATE TABLE $truncateSql"
                        stmt.execute(sql)
                    }

                    for (tableName in tablesFromMetadata) {
                        val snapshotFile = File(snapshotDir, "$tableName.csv")

                        snapshotFile.inputStream().use { input ->
                            val sql = "COPY $tableName FROM STDIN WITH (FORMAT CSV, HEADER)"
                            copyManager.copyIn(sql, input)
                        }
                    }
                    stmt.execute("SET session_replication_role = 'origin';")
                }
                conn.commit()

            } catch (ex: Exception) {
                conn.rollback()
                throw ex
            } finally {
                conn.autoCommit = true
            }
        }
    }

    fun createSnapshotWithCopyManager() {
        snapshotDir.mkdirs()

        val props =
            Properties().apply {
                setProperty("user", username)
                setProperty("password", password)
            }

        DriverManager.getConnection(jdbcUrl, props).use { conn ->
            val copyManager = CopyManager(conn.unwrap(BaseConnection::class.java))

            val tables = getTableNames(conn)

            snapshotMetadataFile.writeText(tables.joinToString("\n"))

            for (tableName in tables) {
                val outputFile = File(snapshotDir, "$tableName.csv")
                if (!outputFile.exists()) {
                    outputFile.outputStream().use { output ->
                        try {
                            // COPY TO streams table data with headers
                            val sql = "COPY $tableName TO STDOUT WITH (FORMAT CSV, HEADER)"
                            copyManager.copyOut(sql, output)
                        } catch (ex: Exception) {
                            log.error(ex) { "Failed to export table $tableName: ${ex.message}" }
                            throw ex
                        }
                    }
                }
            }
        }
    }

    private fun getTableNames(conn: java.sql.Connection): List<String> {
        val tables = mutableListOf<String>()
        val query =
            """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_type = 'BASE TABLE'
            ORDER BY table_name
            """.trimIndent()

        conn.createStatement().use { stmt ->
            stmt.executeQuery(query).use { rs ->
                while (rs.next()) {
                    tables.add(rs.getString("table_name"))
                }
            }
        }

        return tables
    }
}
