import java.sql.DriverManager
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.testcontainers.containers.GenericContainer

abstract class PostgresTestcontainersService :
    BuildService<BuildServiceParameters.None>,
    AutoCloseable {

    @Volatile
    private var postgres: GenericContainer<*>? = null

    @Synchronized
    fun getContainer(): GenericContainer<*> {
        var container = postgres
        if (container == null) {
            container = GenericContainer("postgres:16-alpine").apply {
//                withEnv("POSTGRES_DB", "codex")
                withEnv("POSTGRES_USER", "codex")
                withEnv("POSTGRES_PASSWORD", "codex")
                withExposedPorts(5432)
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
