package com.example.codex

import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
@Transactional
abstract class AbstractIntegrationTest {
    companion object {
        private val useTestcontainers = System.getenv("DISABLE_TESTCONTAINERS") != "true"
        private val postgres =
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("codex")
                withUsername("codex")
                withPassword("codex")
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
                registry.add("spring.datasource.url", postgres::getJdbcUrl)
                registry.add("spring.datasource.username", postgres::getUsername)
                registry.add("spring.datasource.password", postgres::getPassword)
            } else {
                registry.add("spring.datasource.url") {
                    System.getenv("SPRING_DATASOURCE_URL")
                        ?: "jdbc:postgresql://localhost:5432/postgresTest"
                }
                registry.add("spring.datasource.username") { System.getenv("SPRING_DATASOURCE_USERNAME") ?: "postgres" }
                registry.add("spring.datasource.password") { System.getenv("SPRING_DATASOURCE_PASSWORD") ?: "postgres" }
            }
        }
    }
}
