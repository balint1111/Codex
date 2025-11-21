import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.testcontainers.containers.PostgreSQLContainer

class SinglePostgresTestcontainersPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val useTestcontainers = System.getenv("DISABLE_TESTCONTAINERS") != "true"
        if (!useTestcontainers) {
            return
        }

        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("codex")
            withUsername("codex")
            withPassword("codex")
            withReuse(false)
        }

        val startTestcontainers = project.tasks.register("startTestcontainers") {
            doLast {
                if (!postgres.isRunning) {
                    postgres.start()
                }
            }
        }

        val stopTestcontainers = project.tasks.register("stopTestcontainers") {
            doLast {
                if (postgres.isRunning) {
                    postgres.stop()
                }
            }
        }

        project.tasks.withType(Test::class.java).configureEach {
            dependsOn(startTestcontainers)
            finalizedBy(stopTestcontainers)

            doFirst {
                if (!postgres.isRunning) {
                    postgres.start()
                }

                systemProperty("spring.liquibase.url", postgres.jdbcUrl)
                systemProperty("spring.liquibase.user", postgres.username)
                systemProperty("spring.liquibase.password", postgres.password)
                systemProperty("spring.datasource.url", postgres.jdbcUrl)
                systemProperty("spring.datasource.username", postgres.username)
                systemProperty("spring.datasource.password", postgres.password)
                systemProperty("spring.r2dbc.url", postgres.jdbcUrl.replace("jdbc", "r2dbc"))
                systemProperty("spring.r2dbc.username", postgres.username)
                systemProperty("spring.r2dbc.password", postgres.password)
            }
        }

        project.gradle.buildFinished {
            if (postgres.isRunning) {
                postgres.stop()
            }
        }
    }
}
