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

                systemProperty("integration.test.postgres.host", host)
                systemProperty("integration.test.postgres.port", port)
            }
        }
    }
}