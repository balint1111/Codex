package com.example.codex

import com.example.codex.testsupport.DatabaseResetter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import java.util.concurrent.locks.ReentrantLock

@SpringBootTest
abstract class AbstractIntegrationTest {
    protected val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var environment: Environment

    @BeforeEach
    fun beforeEach() {
        databaseLock.lock()
        try {
            val databaseResetter =
                DatabaseResetter(
                    jdbcUrl = resolveProperty("spring.liquibase.url", "spring.datasource.url"),
                    username = resolveProperty("spring.liquibase.user", "spring.datasource.username"),
                    password = resolveProperty("spring.liquibase.password", "spring.datasource.password"),
                )
            databaseResetter.createSnapshotWithCopyManager()
        } catch (ex: Exception) {
            ex.printStackTrace()
            log.error(ex) { "Creating snapshot failed: ${ex.message}" }
        }
    }

    @AfterEach
    fun afterEach() {
        try {
            DatabaseResetter(
                jdbcUrl = resolveProperty("spring.liquibase.url", "spring.datasource.url"),
                username = resolveProperty("spring.liquibase.user", "spring.datasource.username"),
                password = resolveProperty("spring.liquibase.password", "spring.datasource.password"),
            ).resetDatabase()
        } finally {
            databaseLock.unlock()
        }
    }

    private fun resolveProperty(vararg keys: String): String =
        keys.firstNotNullOfOrNull { environment.getProperty(it) }
            ?: error("Missing database property. Checked: ${keys.joinToString()}")

    companion object {
        private val databaseLock = ReentrantLock()
    }
}
