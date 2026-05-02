import nu.studer.gradle.jooq.JooqEdition
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.Property
import java.math.BigDecimal
plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.liquibase.gradle") version "2.2.1"
    id("nu.studer.jooq") version "10.2.1"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.example.codex.testcontainers")
    jacoco
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_25

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.postgresql:r2dbc-postgresql:1.1.1.RELEASE")
    implementation("io.r2dbc:r2dbc-pool")
    implementation("io.projectreactor:reactor-core:3.8.0")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")

    implementation("org.jooq:jooq:3.20.0")
    implementation("org.jooq:jooq-jpa-extensions:3.20.0")

    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    runtimeOnly("org.postgresql:postgresql")
    jooqGenerator("org.postgresql:postgresql")
    jooqGenerator("org.liquibase:liquibase-core")
    jooqGenerator("org.jooq:jooq-meta-extensions-liquibase:3.20.0")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    // Provide liquibase runtime classpath for the Gradle liquibase plugin
    liquibaseRuntime("org.liquibase:liquibase-core")
    liquibaseRuntime("org.postgresql:postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webtestclient")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test:3.8.0")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.postgresql:postgresql")
    liquibaseRuntime("info.picocli:picocli:4.7.5")
}

spotless {
    kotlin {
        target("src/**/*.kt")
        targetExclude("src/main/generated/**")
        ktlint("1.7.1")
    }
    kotlinGradle {
        target("*.kts")
        ktlint("1.7.1")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

// Project Leyden AOT cache - training and consumption for faster test startup
// Cache file location (under build/leyden)
val aotCacheFile = layout.buildDirectory.file("leyden/test-suite.aot")

// 1. Create a helper task to package test classes into a JAR
val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("test")
    from(project.extensions.getByType<SourceSetContainer>().named("test").get().output)
}

val testTraining by tasks.registering(Test::class) {
    group = "verification"
    dependsOn("generateInitSql")
    dependsOn(tasks.jar, testJar)

    val sourceSets = project.extensions.getByType<SourceSetContainer>()
    val main = sourceSets.named("main").get()
    val test = sourceSets.named("test").get()

    testClassesDirs = test.output.classesDirs

    classpath = project.files(
        tasks.jar.get().archiveFile,
        testJar.get().archiveFile,
        test.runtimeClasspath.filter { it.extension == "jar" }
    )

    maxParallelForks = 1
    useJUnitPlatform()
    filter { includeTestsMatching("*ApplicationTest") }

    jvmArgs(
        "-XX:AOTCacheOutput=${aotCacheFile.get().asFile.absolutePath}",
        "-XX:+UnlockDiagnosticVMOptions"
    )

    systemProperty("spring.context.exit", "onRefresh")
    outputs.file(aotCacheFile)
}

tasks.named<Test>("test") {
    dependsOn(testTraining)
    dependsOn("generateInitSql")
    outputs.upToDateWhen { false }

    filter { includeTestsMatching("*IT") }

    doFirst {
        val f = aotCacheFile.get().asFile
        if (f.exists()) {
            if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_25)) {
                logger.warn("Leyden AOT cache detected at ${f.absolutePath} but current JDK (${JavaVersion.current()}) is below the required JDK 25. Skipping AOT cache injection.")
            } else {
                jvmArgs("-XX:AOTCache=${f.absolutePath}")
            }
        } else {
            logger.lifecycle("Leyden AOT cache not found at ${f.absolutePath}. To generate run: ./gradlew testTraining")
        }
    }
}

tasks.matching { it.name != "clean" }.configureEach {
    mustRunAfter("clean")
}

jooq {
    version.set("3.20.0")
    edition.set(JooqEdition.OSS)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.extensions.liquibase.LiquibaseDatabase"
                        properties.add(
                            Property().apply {
                                key = "rootPath"
                                value = "$projectDir/src/main/resources"
                            },
                        )
                        properties.add(
                            Property().apply {
                                key = "scripts"
                                value = "db/changelog/db.changelog-master.yaml"
                            },
                        )
                    }
                    target.apply {
                        packageName = "com.example.codex.jooq"
                        directory = "src/main/generated"
                    }
                    generate.apply {
                        withDeprecated(false)
                        withRecords(true)
                        withImmutablePojos(true)
                        withFluentSetters(true)
                        withJpaVersion("2.2")
                        withJpaAnnotations(true)
                        withImplicitJoinPathsAsKotlinProperties(true)
                        withKotlinSetterJvmNameAnnotationsOnIsPrefix(true)
                        withPojosAsKotlinDataClasses(true)
                        withKotlinNotNullInterfaceAttributes(true)
                        withKotlinNotNullPojoAttributes(true)
                        withKotlinNotNullRecordAttributes(true)
                    }
                }
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks.register("lowercaseJooqNames") {
    val generatedFiles = fileTree("src/main/generated") { include("**/*.kt") }

    inputs.files(generatedFiles)
    outputs.dir("src/main/generated")

    doLast {
        // Regex patterns matching your Perl logic
        val dslNameRegex = Regex("""DSL\.name\("([A-Z_]+)"\)""")
        val nameAssignRegex = Regex("""name = "([A-Z_]+)"""")

        generatedFiles.forEach { file ->
            val originalContent = file.readText()

            // Apply transformations
            val updatedContent = originalContent
                .replace(dslNameRegex) { match ->
                    """DSL.name("${match.groupValues[1].lowercase()}")"""
                }
                .replace(nameAssignRegex) { match ->
                    """name = "${match.groupValues[1].lowercase()}""""
                }

            if (originalContent != updatedContent) {
                file.writeText(updatedContent)
            }
        }
    }
}

tasks.named("generateJooq").configure {
    finalizedBy("lowercaseJooqNames")
}
val liquibaseRuntime = configurations.maybeCreate("liquibaseRuntime")

tasks.register<JavaExec>("generateInitSql") {
    group = "documentation"
    description = "Generates a full init.sql from Liquibase YAML without a DB connection."

    val changelogPath = "src/main/resources/db/changelog/db.changelog-master.yaml"
    val outputFile = layout.buildDirectory.file("init.sql").get().asFile
    val csvFile = layout.projectDirectory.file("databasechangelog.csv").asFile
    inputs.file(changelogPath).withPropertyName("changelogFile")
    outputs.file(outputFile).withPropertyName("generatedSql")

    classpath = liquibaseRuntime
    mainClass.set("liquibase.integration.commandline.Main")

    args(
        "--changelogFile=$changelogPath",
        "--url=offline:postgresql",
        "updateSql"
    )

    doFirst {
        outputFile.parentFile.mkdirs()
        standardOutput = outputFile.outputStream()
    }

    doLast {
        if (outputFile.exists()) {
            val filteredSql = outputFile.readLines()
                .filterNot { it.trimStart().startsWith("--") }
                .filter { it.isNotBlank() }
                .joinToString("\n")

            outputFile.writeText(filteredSql)

            if (csvFile.exists()) {
                csvFile.delete()
                println("Removed temporary tracking file: ${csvFile.name}")
            }
            println("Successfully generated and filtered SQL to: ${outputFile.absolutePath}")
        }
    }
}
