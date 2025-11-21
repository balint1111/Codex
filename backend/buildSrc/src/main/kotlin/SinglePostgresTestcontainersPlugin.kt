import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.testcontainers.containers.PostgreSQLContainer

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

                systemProperty("spring.liquibase.url", postgres.jdbcUrl)
                systemProperty("spring.liquibase.user", postgres.username)
                systemProperty("spring.liquibase.password", postgres.password)
                systemProperty("spring.datasource.url", postgres.jdbcUrl)
                systemProperty("spring.datasource.username", postgres.username)
                systemProperty("spring.datasource.password", postgres.password)
                systemProperty(
                    "spring.r2dbc.url",
                    postgres.jdbcUrl.replace("jdbc", "r2dbc")
                )
                systemProperty("spring.r2dbc.username", postgres.username)
                systemProperty("spring.r2dbc.password", postgres.password)
            }
        }
    }
}