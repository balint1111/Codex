package com.example.codex

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
abstract class AbstractIntegrationTest {
    companion object {
        private val useTestcontainers = System.getenv("DISABLE_TESTCONTAINERS") != "true"
        private val postgres =
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("codex")
                withUsername("codex")
                withPassword("codex")
                withReuse(false)
            }

        @JvmStatic
        @BeforeAll
        fun startContainer() {
            if (useTestcontainers && !postgres.isRunning) {
                postgres.start()
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun datasourceConfig(registry: DynamicPropertyRegistry) {
            if (useTestcontainers) {
                println("${postgres.jdbcUrl} is running with testcontainers")
                registry.add("spring.liquibase.url", postgres::getJdbcUrl)
                registry.add("spring.liquibase.user", postgres::getUsername)
                registry.add("spring.liquibase.password", postgres::getPassword)
                registry.add("spring.r2dbc.url") { postgres.jdbcUrl.replace("jdbc", "r2dbc") }
                registry.add("spring.r2dbc.username", postgres::getUsername)
                registry.add("spring.r2dbc.password", postgres::getPassword)
            } else {
                registry.add("spring.datasource.url") {
                    System.getenv("SPRING_DATASOURCE_URL")
                        ?: "jdbc:postgresql://localhost:5432/postgresTest"
                }
                registry.add("spring.datasource.username") { System.getenv("SPRING_DATASOURCE_USERNAME") ?: "postgres" }
                registry.add("spring.datasource.password") { System.getenv("SPRING_DATASOURCE_PASSWORD") ?: "postgres" }
                registry.add("spring.r2dbc.url") {
                    System.getenv("SPRING_DATASOURCE_URL")
                        ?: "r2dbc:postgresql://localhost:5432/postgresTest"
                }
                registry.add("spring.r2dbc.username") { System.getenv("SPRING_DATASOURCE_USERNAME") ?: "postgres" }
                registry.add("spring.r2dbc.password") { System.getenv("SPRING_DATASOURCE_PASSWORD") ?: "postgres" }
            }
        }
    }

    protected val log = KotlinLogging.logger {}
}
