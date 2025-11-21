package com.example.codex

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
abstract class AbstractIntegrationTest {
    companion object {
        private const val defaultJdbcUrl = "jdbc:postgresql://localhost:5432/postgresTest"
        private const val defaultR2dbcUrl = "r2dbc:postgresql://localhost:5432/postgresTest"

        @JvmStatic
        @DynamicPropertySource
        fun datasourceConfig(registry: DynamicPropertyRegistry) {
            val jdbcUrl =
                System.getProperty("spring.liquibase.url")
                    ?: System.getenv("SPRING_DATASOURCE_URL")
                    ?: defaultJdbcUrl

            val username =
                System.getProperty("spring.liquibase.user")
                    ?: System.getenv("SPRING_DATASOURCE_USERNAME")
                    ?: "postgres"

            val password =
                System.getProperty("spring.liquibase.password")
                    ?: System.getenv("SPRING_DATASOURCE_PASSWORD")
                    ?: "postgres"

            val r2dbcUrl =
                System.getProperty("spring.r2dbc.url")
                    ?: System.getenv("SPRING_DATASOURCE_URL")?.replace("jdbc", "r2dbc")
                    ?: defaultR2dbcUrl

            registry.add("spring.liquibase.url") { jdbcUrl }
            registry.add("spring.liquibase.user") { username }
            registry.add("spring.liquibase.password") { password }
            registry.add("spring.datasource.url") { jdbcUrl }
            registry.add("spring.datasource.username") { username }
            registry.add("spring.datasource.password") { password }
            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username") { username }
            registry.add("spring.r2dbc.password") { password }
        }
    }

    protected val log = KotlinLogging.logger {}
}
