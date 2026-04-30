package com.example.codex.testsupport

import java.net.URI
import java.security.MessageDigest
import java.sql.DriverManager
import java.util.concurrent.ConcurrentHashMap
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory
import org.springframework.test.context.MergedContextConfiguration

class PerTestDatabaseContextCustomizerFactory : ContextCustomizerFactory {
    override fun createContextCustomizer(
        testClass: Class<*>,
        configAttributes: List<ContextConfigurationAttributes>,
    ): ContextCustomizer {
        val databaseName = buildDatabaseName(testClass)
        return PerTestDatabaseContextCustomizer(databaseName)
    }

    private fun buildDatabaseName(testClass: Class<*>): String {
        val jobId = sequenceOf(
            "CI_JOB_ID",
            "GITHUB_RUN_ID",
            "GITHUB_RUN_NUMBER",
            "BUILD_ID",
            "BUILD_NUMBER",
            "TEAMCITY_BUILD_ID",
        ).mapNotNull { System.getenv(it) }.firstOrNull() ?: "local"

        val workerId = System.getProperty("org.gradle.test.worker") ?: "worker"
        val threadId = Thread.currentThread().threadId()
        val raw = "codex_${jobId}_${workerId}_${threadId}_${testClass.name}"

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
            println("database name22: $databaseName")
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
                }
            }
        }

        companion object {
            fun fromEnvironment(context: ConfigurableApplicationContext, databaseName: String): PerTestDatabaseSettings {
                val env = context.environment
                val jdbcUrl =
                    env.getProperty("spring.liquibase.url")
                        ?: System.getProperty("spring.liquibase.url")
                        ?: System.getenv("SPRING_DATASOURCE_URL")
                        ?: DEFAULT_JDBC_URL

                val username =
                    env.getProperty("spring.liquibase.user")
                        ?: System.getProperty("spring.liquibase.user")
                        ?: System.getenv("SPRING_DATASOURCE_USERNAME")
                        ?: DEFAULT_USERNAME

                val password =
                    env.getProperty("spring.liquibase.password")
                        ?: System.getProperty("spring.liquibase.password")
                        ?: System.getenv("SPRING_DATASOURCE_PASSWORD")
                        ?: DEFAULT_PASSWORD

                val parsed = parseJdbcUrl(jdbcUrl)
                val jdbcWithDatabase = buildJdbcUrl(parsed.host, parsed.port, databaseName, parsed.query)
                val r2dbcWithDatabase = buildR2dbcUrl(parsed.host, parsed.port, databaseName, parsed.query)

                return PerTestDatabaseSettings(
                    jdbcUrl = jdbcWithDatabase,
                    r2dbcUrl = r2dbcWithDatabase,
                    username = username,
                    password = password,
                    host = parsed.host,
                    port = parsed.port,
                    databaseName = databaseName,
                )
            }

            private fun parseJdbcUrl(jdbcUrl: String): JdbcUrlParts {
                val trimmed = jdbcUrl.removePrefix("jdbc:")
                val uri = URI(trimmed)
                val host = uri.host ?: DEFAULT_HOST
                val port = if (uri.port == -1) DEFAULT_PORT else uri.port
                val query = uri.query?.let { "?$it" } ?: ""
                return JdbcUrlParts(host, port, query)
            }

            private fun buildJdbcUrl(host: String, port: Int, database: String, query: String): String {
                return "jdbc:postgresql://$host:$port/$database$query"
            }

            private fun buildR2dbcUrl(host: String, port: Int, database: String, query: String): String {
                return "r2dbc:postgresql://$host:$port/$database$query"
            }
        }
    }

    private data class JdbcUrlParts(
        val host: String,
        val port: Int,
        val query: String,
    )

    companion object {
        private const val DEFAULT_JDBC_URL = "jdbc:postgresql://localhost:5432/postgresTest"
        private const val DEFAULT_HOST = "localhost"
        private const val DEFAULT_PORT = 5432
        private const val DEFAULT_USERNAME = "postgres"
        private const val DEFAULT_PASSWORD = "postgres"
        private const val MAX_DB_NAME_LENGTH = 63
        private const val HASH_SUFFIX_LENGTH = 9

        private val createdDatabases = ConcurrentHashMap.newKeySet<String>()
    }
}
