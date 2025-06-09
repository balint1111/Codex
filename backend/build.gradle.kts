import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.liquibase.gradle.LiquibaseExtension
plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.1.21"
    id("org.springframework.boot") version "3.1.1"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.liquibase.gradle") version "2.2.1"
    id("nu.studer.jooq") version "10.1"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-jooq")

    implementation("org.jooq:jooq:3.18.4")
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.h2database:h2")
    jooqGenerator("com.h2database:h2")
    implementation("org.liquibase:liquibase-core")
    liquibaseRuntime("org.liquibase:liquibase-core")
    liquibaseRuntime("com.h2database:h2")
    liquibaseRuntime("org.postgresql:postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}


tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

liquibase {
    activities.register("jooq") {
        arguments = mapOf(
            "changeLogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
            "url" to "jdbc:h2:mem:codex_jooq;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false",
            "driver" to "org.h2.Driver"
        ).toMutableMap()
    }
}

val liquibaseExt = extensions.getByType<org.liquibase.gradle.LiquibaseExtension>()

tasks.register("updateJooq") {
    doFirst { liquibaseExt.runList = "jooq" }
    finalizedBy("update")
    doLast { liquibaseExt.runList = null }
}

tasks.register("dropAllJooq") {
    doFirst { liquibaseExt.runList = "jooq" }
    finalizedBy("dropAll")
    doLast { liquibaseExt.runList = null }
}


jooq {
    version.set("3.18.4")
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.h2.Driver"
                    url = "jdbc:h2:mem:codex_jooq;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
                    user = "sa"
                    password = ""
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.h2.H2Database"
                        inputSchema = "PUBLIC"
                    }
                    target.apply {
                        packageName = "com.example.codex.jooq"
                        directory = "src/generated/kotlin"
                    }
                }
            }
        }
    }
}

tasks.named("generateJooq").configure {
    dependsOn("updateJooq")
    finalizedBy("dropAllJooq")
}
