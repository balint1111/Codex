package com.example.codex

import com.example.codex.testsupport.DatabaseResetter
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment

@SpringBootTest
abstract class AbstractIntegrationTest {
    @Autowired
    private lateinit var environment: Environment

    @AfterEach
    fun afterEach() {
        DatabaseResetter(
            jdbcUrl = resolveProperty("spring.liquibase.url", "spring.datasource.url"),
            username = resolveProperty("spring.liquibase.user", "spring.datasource.username"),
            password = resolveProperty("spring.liquibase.password", "spring.datasource.password"),
        ).resetDatabase()
    }

    private fun resolveProperty(vararg keys: String): String =
        keys.firstNotNullOfOrNull { environment.getProperty(it) }
            ?: error("Missing database property. Checked: ${keys.joinToString()}")
}
