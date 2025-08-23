package com.example.codex

import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.DriverManager
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
abstract class AbstractIntegrationTest {
    companion object {
        private val useTestcontainers = System.getenv("DISABLE_TESTCONTAINERS") != "true"
        private val postgres =
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("codex")
                withUsername("codex")
                withPassword("codex")
            }
        private val counter = AtomicInteger()

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
            val db = "testdb_${counter.incrementAndGet()}"
            val schema = "tests_${UUID.randomUUID().toString().replace("-", "")}" 
            if (useTestcontainers) {
                postgres.createConnection("").use { conn ->
                    conn.createStatement().use { it.execute("CREATE DATABASE $db") }
                }
                val jdbcUrl = postgres.jdbcUrl.replace("codex", db)
                DriverManager.getConnection(jdbcUrl, postgres.username, postgres.password).use { conn ->
                    conn.createStatement().use { it.execute("CREATE SCHEMA $schema") }
                }
                registry.add("spring.datasource.url") { "$jdbcUrl?currentSchema=$schema" }
                registry.add("spring.datasource.username", postgres::getUsername)
                registry.add("spring.datasource.password", postgres::getPassword)
            } else {
                val baseUrl =
                    System.getenv("SPRING_DATASOURCE_URL") ?: "jdbc:postgresql://localhost:5432/postgresTest"
                val username = System.getenv("SPRING_DATASOURCE_USERNAME") ?: "postgres"
                val password = System.getenv("SPRING_DATASOURCE_PASSWORD") ?: "postgres"
                DriverManager.getConnection(baseUrl, username, password).use { conn ->
                    conn.createStatement().use { it.execute("CREATE SCHEMA $schema") }
                }
                registry.add("spring.datasource.url") { "$baseUrl?currentSchema=$schema" }
                registry.add("spring.datasource.username") { username }
                registry.add("spring.datasource.password") { password }
            }
        }
    }
}
