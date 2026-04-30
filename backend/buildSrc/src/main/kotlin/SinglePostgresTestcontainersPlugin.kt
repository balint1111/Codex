import java.security.MessageDigest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class SinglePostgresTestcontainersPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val useTestcontainersProvider = project.providers
            .environmentVariable("DISABLE_TESTCONTAINERS")
            .map { it != "true" }
            .orElse(true)

        if (!useTestcontainersProvider.get()) {
            return
        }

        val postgresServiceProvider = project.gradle.sharedServices
            .registerIfAbsent(
                "singlePostgresTestcontainers",
                PostgresTestcontainersService::class.java
            )

        project.tasks.withType(Test::class.java).configureEach {
            usesService(postgresServiceProvider)

            doFirst {
                val postgresService = postgresServiceProvider.get()
                val postgres = postgresService.getContainer()

                val host = postgres.host
                val port = postgres.getMappedPort(5432)
                val username = "codex"
                val password = "codex"
                val database = buildDatabaseName(path)
                println("database name: $database")

                postgresService.ensureDatabase(host, port, username, password, database)

                val jdbcUrl = "jdbc:postgresql://$host:$port/$database"
                val r2dbcUrl = "r2dbc:postgresql://$host:$port/$database"

                systemProperty("spring.liquibase.url", jdbcUrl)
                systemProperty("spring.liquibase.user", username)
                systemProperty("spring.liquibase.password", password)
                systemProperty("spring.datasource.url", jdbcUrl)
                systemProperty("spring.datasource.username", username)
                systemProperty("spring.datasource.password", password)
                systemProperty("spring.r2dbc.url", r2dbcUrl)
                systemProperty("spring.r2dbc.username", username)
                systemProperty("spring.r2dbc.password", password)
            }
        }
    }

    private fun buildDatabaseName(taskPath: String): String {
        val jobId = sequenceOf(
            "CI_JOB_ID",
            "GITHUB_RUN_ID",
            "GITHUB_RUN_NUMBER",
            "BUILD_ID",
            "BUILD_NUMBER",
            "TEAMCITY_BUILD_ID"
        ).mapNotNull { System.getenv(it) }.firstOrNull() ?: "local"

        val raw = "codex_${jobId}_$taskPath"
        val sanitized = raw
            .lowercase()
            .replace(Regex("[^a-z0-9_]+"), "_")
            .trim('_')

        if (sanitized.length <= 63 && sanitized.isNotBlank()) {
            return sanitized
        }

        val hash = MessageDigest.getInstance("MD5")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .substring(0, 8)

        val base = sanitized.ifBlank { "codex" }.take(63 - 9)
        return "${base}_$hash"
    }
}