package com.example.codex

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.DriverManager
import java.util.UUID

@SpringBootTest
@ContextConfiguration(initializers = [AbstractIntegrationTest.Companion.Initializer::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
abstract class AbstractIntegrationTest {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var env: Environment

    companion object {
        private val useTestcontainers = System.getenv("DISABLE_TESTCONTAINERS") != "true"

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(context: ConfigurableApplicationContext) {
                val schema = "test_${UUID.randomUUID().toString().replace("-", "")}" 
                if (useTestcontainers) {
                    val postgres =
                        PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                            withDatabaseName("codex")
                            withUsername("codex")
                            withPassword("codex")
                            start()
                        }
                    createSchema(postgres.jdbcUrl, postgres.username, postgres.password, schema)
                    TestPropertyValues.of(
                        "spring.datasource.url=${postgres.jdbcUrl}?currentSchema=$schema",
                        "spring.datasource.username=${postgres.username}",
                        "spring.datasource.password=${postgres.password}",
                        "test.schema=$schema",
                    ).applyTo(context.environment)
                    context.beanFactory.registerSingleton("postgresContainer", postgres)
                } else {
                    val url = System.getenv("SPRING_DATASOURCE_URL") ?: "jdbc:postgresql://localhost:5432/postgresTest"
                    val username = System.getenv("SPRING_DATASOURCE_USERNAME") ?: "postgres"
                    val password = System.getenv("SPRING_DATASOURCE_PASSWORD") ?: "postgres"
                    createSchema(url, username, password, schema)
                    val sep = if (url.contains("?")) "&" else "?"
                    TestPropertyValues.of(
                        "spring.datasource.url=$url${sep}currentSchema=$schema",
                        "spring.datasource.username=$username",
                        "spring.datasource.password=$password",
                        "test.schema=$schema",
                    ).applyTo(context.environment)
                }
            }

            private fun createSchema(url: String, username: String, password: String, schema: String) {
                DriverManager.getConnection(url, username, password).use { conn ->
                    conn.createStatement().use { stmt ->
                        stmt.execute("CREATE SCHEMA IF NOT EXISTS $schema")
                    }
                }
            }
        }
    }
}

