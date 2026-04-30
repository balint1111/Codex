package com.example.codex.testsupport

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.Connection
import java.sql.DriverManager

class DatabaseResetter(
    private val jdbcUrl: String,
    private val username: String,
    private val password: String,
) {
    fun resetSeedData(changeLogPath: String, seedContext: String) {
        DriverManager.getConnection(jdbcUrl, username, password).use { connection ->
            truncateAllTables(connection)
            clearSeedChangeSets(connection, seedContext)
            runSeedLiquibase(connection, changeLogPath, seedContext)
        }
    }

    private fun truncateAllTables(connection: Connection) {
        val tables = mutableListOf<String>()
        connection.createStatement().use { statement ->
            statement.executeQuery("select tablename from pg_tables where schemaname = 'public'").use { rs ->
                while (rs.next()) {
                    val tableName = rs.getString(1)
                    if (!isLiquibaseTable(tableName)) {
                        tables.add("\"$tableName\"")
                    }
                }
            }
        }

        if (tables.isEmpty()) {
            return
        }

        connection.createStatement().use { statement ->
            statement.execute("TRUNCATE TABLE ${tables.joinToString(", ")} RESTART IDENTITY CASCADE")
        }
    }

    private fun clearSeedChangeSets(connection: Connection, seedContext: String) {
        connection.prepareStatement(
            "delete from databasechangelog where contexts like ?"
        ).use { statement ->
            statement.setString(1, "%$seedContext%")
            statement.executeUpdate()
        }
    }

    private fun runSeedLiquibase(connection: Connection, changeLogPath: String, seedContext: String) {
        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(connection))
        val liquibase = Liquibase(changeLogPath, ClassLoaderResourceAccessor(), database)
        liquibase.update(Contexts(seedContext), LabelExpression())
    }

    private fun isLiquibaseTable(tableName: String): Boolean {
        return tableName.equals("databasechangelog", ignoreCase = true) ||
            tableName.equals("databasechangeloglock", ignoreCase = true)
    }
}


