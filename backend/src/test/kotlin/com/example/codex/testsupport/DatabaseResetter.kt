package com.example.codex.testsupport

import io.github.oshai.kotlinlogging.KotlinLogging
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import java.io.File
import java.sql.DriverManager
import java.util.*

class DatabaseResetter(
    private val jdbcUrl: String,
    private val username: String,
    private val password: String,
) {
    companion object {
        private val snapshotDir = File("build/test-snapshot")
        private val snapshotMetadataFile = File("build/test-snapshot/tables.txt")
        private const val CHANGE_LOG_PATH = "db/changelog/db.changelog-master.yaml"
        private val log = KotlinLogging.logger {}
    }

    fun resetDatabase() {
        // If a snapshot exists, truncate tables and restore from snapshot
        if (snapshotDir.exists() && snapshotMetadataFile.exists()) {
            try {
                recreateDatabaseFromSnapshot()
                return
            } catch (ex: Exception) {
                log.warn(ex) { "Snapshot restore failed: ${ex.message}. Falling back to Liquibase." }
            }
        }
    }

    private fun recreateDatabaseFromSnapshot() {
        // truncate all non-liquibase tables
        val props =
            Properties().apply {
                setProperty("user", username)
                setProperty("password", password)
            }

        DriverManager.getConnection(jdbcUrl, props).use { conn ->
            truncateAllTablesExceptLiquibase(conn)
        }

        // restore data from snapshot
        restoreDataFromSnapshot()
    }

    private fun truncateAllTablesExceptLiquibase(conn: java.sql.Connection) {
        val tablesToTruncate = getTableNames(conn)

        conn.createStatement().use { stmt ->
            for (tableName in tablesToTruncate) {
                try {
                    stmt.execute("TRUNCATE TABLE $tableName RESTART IDENTITY CASCADE")
                } catch (ex: Exception) {
                    log.warn(ex) { "Failed to truncate table $tableName: ${ex.message}" }
                }
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
            val copyManager = CopyManager(conn.unwrap(BaseConnection::class.java))

            // Read the list of tables from metadata
            val tables = snapshotMetadataFile.readLines().filter { it.isNotBlank() }

            for (tableName in tables) {
                val snapshotFile = File(snapshotDir, "$tableName.csv")
                if (!snapshotFile.exists()) {
                    log.warn { "Snapshot file not found for table $tableName" }
                    continue
                }

                snapshotFile.inputStream().use { input ->
                    println("input: " + String(input.readAllBytes()))
                    try {
                        copyManager.copyIn("COPY $tableName FROM STDIN WITH (FORMAT CSV, HEADER)", input)
                    } catch (ex: Exception) {
                        log.error(ex) { "Failed to restore table $tableName: ${ex.message}" }
                        throw ex
                    }
                }
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
                            copyManager.copyOut("COPY $tableName TO STDOUT WITH (FORMAT CSV, HEADER)", output)
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
