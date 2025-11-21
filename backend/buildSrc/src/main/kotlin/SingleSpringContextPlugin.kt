import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class SingleSpringContextPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.withType(Test::class.java).configureEach {
            // Limit Spring's context cache to a single entry so the same application context is reused
            // across all test classes.
            systemProperty("spring.test.context.cache.maxSize", "1")
        }
    }
}
