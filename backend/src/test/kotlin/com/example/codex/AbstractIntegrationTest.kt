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
            println("before liquibase")
            databaseResetter.runLiquibase()
            println("after liquibase")
            println("before createSnapshotWithCopyManager")
            databaseResetter.createSnapshotWithCopyManager()
            println("after createSnapshotWithCopyManager")
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("Creating snapshot failed: ${ex.message}. Tests will continue using Liquibase-based reseed.")
        }
    }

    @AfterEach
    fun afterEach() {
        try {
            println("before resetting database")
            DatabaseResetter(
                jdbcUrl = resolveProperty("spring.liquibase.url", "spring.datasource.url"),
                username = resolveProperty("spring.liquibase.user", "spring.datasource.username"),
                password = resolveProperty("spring.liquibase.password", "spring.datasource.password"),
            ).resetDatabase()
            println("after resetting database")
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
