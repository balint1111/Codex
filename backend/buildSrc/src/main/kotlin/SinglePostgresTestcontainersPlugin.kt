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
                val postgres = postgresServiceProvider.get().getContainer()

                val host = postgres.host
                val port = postgres.getMappedPort(5432)
                val username = "codex"
                val password = "codex"
                val database = "codex"

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
}