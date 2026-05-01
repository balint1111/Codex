package com.example.codex.testsupport

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import java.io.File
import java.sql.DriverManager
import java.util.Properties

class DatabaseResetter(
    private val jdbcUrl: String,
    private val username: String,
    private val password: String,
) {
    companion object {
        private val snapshotDir = File("build/test-snapshot")
        private val snapshotMetadataFile = File("build/test-snapshot/tables.txt")
        private const val CHANGE_LOG_PATH = "db/changelog/db.changelog-master.yaml"
    }

    fun resetDatabase() {
        // If a snapshot exists, truncate tables and restore from snapshot
        if (snapshotDir.exists() && snapshotMetadataFile.exists()) {
            println("Restoring from snapshot")
            try {
                recreateDatabaseFromSnapshot()
                return
            } catch (ex: Exception) {
                println("Snapshot restore failed: ${ex.message}. Falling back to Liquibase.")
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
            .filter { !it.equals("databasechangelog", ignoreCase = true) &&
                    !it.equals("databasechangeloglock", ignoreCase = true) }

        conn.createStatement().use { stmt ->
            for (tableName in tablesToTruncate) {
                try {
                    stmt.execute("TRUNCATE TABLE $tableName RESTART IDENTITY CASCADE")
                    println("Truncated table: $tableName")
                } catch (ex: Exception) {
                    println("Warning: Failed to truncate table $tableName: ${ex.message}")
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
                    println("Warning: Snapshot file not found for table $tableName")
                    continue
                }

                snapshotFile.inputStream().use { input ->
                    try {
                        // COPY FROM restores data with header row
                        copyManager.copyIn("COPY $tableName FROM STDIN WITH (FORMAT CSV, HEADER)", input)
                        println("Restored table: $tableName")
                    } catch (ex: Exception) {
                        println("Failed to restore table $tableName: ${ex.message}")
                        throw ex
                    }
                }
            }
        }
    }

    fun createSnapshotWithCopyManager() {
        snapshotDir.mkdirs()
        println("Creating snapshot in: ${snapshotDir.absolutePath}")

        val props =
            Properties().apply {
                setProperty("user", username)
                setProperty("password", password)
            }

        DriverManager.getConnection(jdbcUrl, props).use { conn ->
            val copyManager = CopyManager(conn.unwrap(BaseConnection::class.java))

            // Get all table names from the database
            val tables = getTableNames(conn)

            // Write table names to metadata file
            snapshotMetadataFile.writeText(tables.joinToString("\n"))
            println("Found ${tables.size} tables: $tables")

            // Export each table to CSV format
            for (tableName in tables) {
                val outputFile = File(snapshotDir, "$tableName.csv")
                outputFile.outputStream().use { output ->
                    try {
                        // COPY TO streams table data with headers
                        copyManager.copyOut("COPY $tableName TO STDOUT WITH (FORMAT CSV, HEADER)", output)
                        println("Exported table: $tableName to ${outputFile.name}")
                    } catch (ex: Exception) {
                        println("Failed to export table $tableName: ${ex.message}")
                        throw ex
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

    fun runLiquibase() {
        DriverManager.getConnection(jdbcUrl, username, password).use { connection ->
            val database =
                DatabaseFactory
                    .getInstance()
                    .findCorrectDatabaseImplementation(JdbcConnection(connection))
            val liquibase = Liquibase(CHANGE_LOG_PATH, ClassLoaderResourceAccessor(), database)
            liquibase.update()
        }
    }
}
