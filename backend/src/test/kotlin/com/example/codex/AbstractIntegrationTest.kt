package com.example.codex

import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import liquibase.integration.spring.SpringLiquibase
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@Import(TestDatabaseConfig::class)
abstract class AbstractIntegrationTest {
    companion object {
        private val useTestcontainers = System.getenv("DISABLE_TESTCONTAINERS") != "true"
        private val postgres =
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("codex")
                withUsername("codex")
                withPassword("codex")
            }
        private val liquibaseLock = Any()

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

    @Autowired
    protected lateinit var dslContext: DSLContext

    @Autowired
    private lateinit var liquibase: SpringLiquibase

    private val schemaCounter = AtomicInteger()

    @BeforeEach
    fun setupSchema() {
        val schema = "test_schema_${schemaCounter.incrementAndGet()}"

        SchemaHolder.current.remove()
        dslContext.execute("create schema if not exists \"$schema\"")
        synchronized(liquibaseLock) {
            liquibase.defaultSchema = schema
            liquibase.afterPropertiesSet()
        }
        SchemaHolder.current.set(schema)
    }

    @AfterEach
    fun cleanupSchema() {
        SchemaHolder.current.get()?.let { schema ->
            dslContext.execute("drop schema if exists \"$schema\" cascade")
        }
        SchemaHolder.current.remove()
    }
}
