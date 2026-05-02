package com.example.codex.testsupport

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory
import org.springframework.test.context.MergedContextConfiguration
import java.security.MessageDigest
import java.sql.DriverManager
import java.util.concurrent.ConcurrentHashMap

class PerTestDatabaseContextCustomizerFactory : ContextCustomizerFactory {
    override fun createContextCustomizer(
        testClass: Class<*>,
        configAttributes: List<ContextConfigurationAttributes>,
    ): ContextCustomizer {
        val databaseName = buildDatabaseName(testClass)
        return PerTestDatabaseContextCustomizer(databaseName)
    }

    private fun buildDatabaseName(testClass: Class<*>): String {
        val jobId =
            sequenceOf(
                "CI_JOB_ID",
                "GITHUB_RUN_ID",
                "GITHUB_RUN_NUMBER",
                "BUILD_ID",
                "BUILD_NUMBER",
                "TEAMCITY_BUILD_ID",
            ).firstNotNullOfOrNull { System.getenv(it) } ?: "local"

        val workerId = System.getProperty("org.gradle.test.worker") ?: "worker"
        val raw = "codex_${jobId}_${workerId}_${testClass.simpleName}"

        val sanitized =
            raw
                .lowercase()
                .replace(Regex("[^a-z0-9_]+"), "_")
                .trim('_')

        if (sanitized.length <= MAX_DB_NAME_LENGTH && sanitized.isNotBlank()) {
            return sanitized
        }

        val hash =
            MessageDigest
                .getInstance("MD5")
                .digest(raw.toByteArray())
                .joinToString("") { "%02x".format(it) }
                .substring(0, 8)

        val base = sanitized.ifBlank { "codex" }.take(MAX_DB_NAME_LENGTH - HASH_SUFFIX_LENGTH)
        return "${base}_$hash"
    }

    private class PerTestDatabaseContextCustomizer(
        private val databaseName: String,
    ) : ContextCustomizer {
        override fun customizeContext(
            context: ConfigurableApplicationContext,
            mergedConfig: MergedContextConfiguration,
        ) {
            val settings = PerTestDatabaseSettings.fromEnvironment(context, databaseName)
            settings.ensureDatabase()

            TestPropertyValues
                .of(
                    "spring.liquibase.url=${settings.jdbcUrl}",
                    "spring.liquibase.user=${settings.username}",
                    "spring.liquibase.password=${settings.password}",
                    "spring.datasource.url=${settings.jdbcUrl}",
                    "spring.datasource.username=${settings.username}",
                    "spring.datasource.password=${settings.password}",
                    "spring.r2dbc.url=${settings.r2dbcUrl}",
                    "spring.r2dbc.username=${settings.username}",
                    "spring.r2dbc.password=${settings.password}",
                ).applyTo(context.environment)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is PerTestDatabaseContextCustomizer) {
                return false
            }
            return databaseName == other.databaseName
        }

        override fun hashCode(): Int = databaseName.hashCode()
    }

    private data class PerTestDatabaseSettings(
        val jdbcUrl: String,
        val r2dbcUrl: String,
        val username: String,
        val password: String,
        val host: String,
        val port: Int,
        val databaseName: String,
    ) {
        fun ensureDatabase() {
            if (!createdDatabases.add(databaseName)) {
                return
            }

            val adminJdbcUrl = "jdbc:postgresql://$host:$port/postgres"
            DriverManager.getConnection(adminJdbcUrl, username, password).use { connection ->
                connection.prepareStatement("select 1 from pg_database where datname = ?").use { select ->
                    select.setString(1, databaseName)
                    select.executeQuery().use { rs ->
                        if (rs.next()) {
                            return
                        }
                    }
                }

                connection.createStatement().use { statement ->
                    statement.execute("create database \"$databaseName\"")
                        // After creating the database, execute generated init SQL (if present) to populate schema
                        try {
                            runInitSql(host, port, databaseName, username, password)
                        } catch (ex: Exception) {
                            // Log but do not fail database creation; tests may still run with Liquibase
                            println("Warning: running init SQL failed: ${ex.message}")
                        }
                }
            }
        }

        private fun runInitSql(host: String, port: Int, database: String, username: String, password: String) {
            DriverManager.getConnection(buildJdbcUrl(host, port, database), username, password).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(java.io.File("build/init.sql").readText())
                }
            }
        }

        companion object {
            fun fromEnvironment(
                context: ConfigurableApplicationContext,
                databaseName: String,
            ): PerTestDatabaseSettings {
                val env = context.environment
                val host =
                    env.getProperty("integration.test.postgres.host")
                        ?: System.getProperty("integration.test.postgres.host") ?: DEFAULT_HOST
                val port =
                    (
                        env.getProperty("integration.test.postgres.port")
                            ?: System.getProperty("integration.test.postgres.port")
                    )?.toInt() ?: DEFAULT_PORT

                val username = DEFAULT_USERNAME
                val password = DEFAULT_PASSWORD

                val jdbcWithDatabase = buildJdbcUrl(host, port, databaseName)
                val r2dbcWithDatabase = buildR2dbcUrl(host, port, databaseName)

                return PerTestDatabaseSettings(
                    jdbcUrl = jdbcWithDatabase,
                    r2dbcUrl = r2dbcWithDatabase,
                    username = username,
                    password = password,
                    host = host,
                    port = port,
                    databaseName = databaseName,
                )
            }

            private fun buildJdbcUrl(
                host: String,
                port: Int,
                database: String,
            ): String = "jdbc:postgresql://$host:$port/$database"

            private fun buildR2dbcUrl(
                host: String,
                port: Int,
                database: String,
            ): String = "r2dbc:postgresql://$host:$port/$database"
        }
    }

    companion object {
        private const val DEFAULT_HOST = "localhost"
        private const val DEFAULT_PORT = 5432
        private const val DEFAULT_USERNAME = "codex"
        private const val DEFAULT_PASSWORD = "codex"
        private const val MAX_DB_NAME_LENGTH = 63
        private const val HASH_SUFFIX_LENGTH = 9

        private val createdDatabases = ConcurrentHashMap.newKeySet<String>()
    }
}
