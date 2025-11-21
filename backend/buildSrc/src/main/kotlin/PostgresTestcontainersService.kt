import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.testcontainers.containers.PostgreSQLContainer

abstract class PostgresTestcontainersService :
    BuildService<BuildServiceParameters.None>,
    AutoCloseable {

    @Volatile
    private var postgres: PostgreSQLContainer<Nothing>? = null

    @Synchronized
    fun getContainer(): PostgreSQLContainer<Nothing> {
        var container = postgres
        if (container == null) {
            container = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("codex")
                withUsername("codex")
                withPassword("codex")
                withReuse(false)
            }
            postgres = container
        }

        if (!container.isRunning) {
            container.start()
        }

        return container
    }

    override fun close() {
        postgres?.let {
            if (it.isRunning) {
                it.stop()
            }
        }
    }
}
