package com.example.codex

import java.sql.DriverManager
import java.util.UUID
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
abstract class AbstractIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15")

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            val schema = "test_${UUID.randomUUID().toString().replace("-", "")}".also {
                DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password).use { conn ->
                    conn.createStatement().use { stmt ->
                        stmt.execute("CREATE SCHEMA \"$it\"")
                    }
                }
            }

            registry.add("spring.datasource.url") { "${postgres.jdbcUrl}?currentSchema=$schema" }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.liquibase.default-schema") { schema }
        }
    }
}
