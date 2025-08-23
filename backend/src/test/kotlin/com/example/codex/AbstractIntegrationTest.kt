package com.example.codex

import jakarta.annotation.PostConstruct
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import liquibase.integration.spring.SpringLiquibase
import java.util.UUID
import javax.sql.DataSource

@SpringBootTest
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

    @SpyBean
    protected lateinit var dataSource: DataSource

    @SpyBean
    protected lateinit var dslContext: DSLContext

    @PostConstruct
    fun initDataSourceSpy() {
        Mockito.doAnswer { invocation ->
            val conn = invocation.callRealMethod() as java.sql.Connection
            val schema = SchemaContext.get()
            conn.createStatement().use { it.execute("set search_path to \"$schema\"") }
            conn
        }.`when`(dataSource).connection
    }

    @BeforeEach
    fun prepareSchema(testInfo: TestInfo) {
        val schema = "test_" + UUID.randomUUID().toString().replace("-", "")
        SchemaContext.set("public")
        dslContext.execute("create schema if not exists \"$schema\"")
        SchemaContext.set(schema)
        SpringLiquibase().apply {
            changeLog = "classpath:db/changelog/db.changelog-master.yaml"
            dataSource = this@AbstractIntegrationTest.dataSource
            defaultSchema = schema
            afterPropertiesSet()
        }
    }
}
