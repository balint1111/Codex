import nu.studer.gradle.jooq.JooqEdition
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.Property

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    alias(libs.plugins.liquibaseGradle)
    alias(libs.plugins.nuStuderJooq)
    alias(libs.plugins.spotless)
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
    implementation(libs.springBootStarterWebflux)
    implementation(libs.springBootStarterSecurity)
    implementation(libs.springBootStarterOauth2ResourceServer)

    implementation(libs.springBootStarterDataR2dbc)
    implementation(libs.r2dbcPostgresql)

    implementation(libs.springdocOpenapiStarterWebfluxUi)

    implementation(libs.jooq)
    implementation(libs.jooqJpaExtensions)

    implementation(libs.jakartaPersistenceApi)
    runtimeOnly(libs.postgresqlDriver)
    jooqGenerator(libs.postgresqlDriver)
    jooqGenerator(libs.liquibaseCore)
    jooqGenerator(libs.jooqCodegen)
    jooqGenerator(libs.jooqMeta)
    jooqGenerator(libs.jooq)
    jooqGenerator(libs.jooqMetaExtensionsLiquibase)
    // implementation(libs.liquibaseCore) // redundant with spring-boot-starter-liquibase
    implementation(libs.springBootStarterLiquibase)
    liquibaseRuntime(libs.liquibaseCore)
    liquibaseRuntime(libs.postgresqlDriver)
    implementation(libs.kotlinReflect)
    implementation(libs.kotlinStdlibJdk8)
    implementation(libs.kotlinLoggingJvm)
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.springBootWebtestclient)
    testImplementation(libs.springSecurityTest)
    testImplementation(libs.reactorTest)
    // testImplementation(libs.postgresqlDriver) // removed: rely on runtime/postgres driver or testcontainers-provided driver
    liquibaseRuntime(libs.picocli)
}

spotless {
    kotlin {
        target("src/**/*.kt")
        targetExclude("src/main/generated/**")
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target("*.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = 1
}

val lowercaseJooqNames by tasks.registering {
    val generatedFiles = fileTree("src/main/generated") { include("**/*.kt") }

    inputs.files(generatedFiles)
    outputs.dir("src/main/generated")

    doLast {
        val dslNameRegex = Regex("""DSL\.name\("([A-Z_]+)"\)""")
        val nameAssignRegex = Regex("""name = "([A-Z_]+)"""")

        generatedFiles.forEach { file ->
            val originalContent = file.readText()

            val updatedContent =
                originalContent
                    .replace(dslNameRegex) { match ->
                        """DSL.name("${match.groupValues[1].lowercase()}")"""
                    }.replace(nameAssignRegex) { match ->
                        """name = "${match.groupValues[1].lowercase()}""""
                    }

            if (originalContent != updatedContent) {
                file.writeText(updatedContent)
            }
        }
    }
}

val generateInitSql by tasks.registering(JavaExec::class) {
    group = "documentation"
    description = "Generates a full init.sql from Liquibase YAML without a DB connection."

    val changelogPath = "src/main/resources/db/changelog/db.changelog-master.yaml"
    val outputFile =
        layout.buildDirectory
            .file("init.sql")
            .get()
            .asFile
    val csvFile = layout.projectDirectory.file("databasechangelog.csv").asFile
    inputs.file(changelogPath).withPropertyName("changelogFile")
    outputs.file(outputFile).withPropertyName("generatedSql")

    classpath = liquibaseRuntime
    mainClass.set("liquibase.integration.commandline.Main")

    args(
        "--changelogFile=$changelogPath",
        "--url=offline:postgresql",
        "updateSql",
    )

    environment("LIQUIBASE_SHOW_BANNER", "false")

    doFirst {
        outputFile.parentFile.mkdirs()
        standardOutput = outputFile.outputStream()
    }

    doLast {
        if (outputFile.exists()) {
            val filteredSql =
                outputFile
                    .readLines()
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

val aotCacheFile = layout.buildDirectory.file("leyden/test-suite.aot")

val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("test")
    from(
        project.extensions
            .getByType<SourceSetContainer>()
            .named("test")
            .get()
            .output,
    )
}

val testTraining by tasks.registering(Test::class) {
    group = "verification"
    dependsOn(generateInitSql)
    dependsOn(tasks.jar, testJar)

    val sourceSets = project.extensions.getByType<SourceSetContainer>()
    val test = sourceSets.named("test").get()

    testClassesDirs = test.output.classesDirs

    classpath =
        project.files(
            tasks.jar.get().archiveFile,
            testJar.get().archiveFile,
            test.runtimeClasspath.filter { it.extension == "jar" },
        )

    filter { includeTestsMatching("*ApplicationTest") }

    jvmArgs(
        "-XX:AOTCacheOutput=${aotCacheFile.get().asFile.absolutePath}",
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:+UseCompactObjectHeaders",
        "-Xshare:off",
        "-Dorg.jooq.no-logo=true",
        "-XX:+EnableDynamicAgentLoading",
    )

    systemProperty("spring.context.exit", "onRefresh")
    outputs.file(aotCacheFile)
}

val aotEnabledInTest = false
tasks.named<Test>("test") {
    if (aotEnabledInTest) {
        dependsOn(testTraining)
    }
    dependsOn(generateInitSql)
    outputs.upToDateWhen { false }

    filter { includeTestsMatching("*IT") }

    doFirst {
        if (!aotEnabledInTest) {
            jvmArgs("-Xshare:off", "-XX:+UseCompactObjectHeaders", "-Dorg.jooq.no-logo=true", "-XX:+EnableDynamicAgentLoading")
            return@doFirst
        }
        val f = aotCacheFile.get().asFile
        if (f.exists()) {
            if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_25)) {
                logger.warn(
                    "Leyden AOT cache detected at ${f.absolutePath} but current JDK (${JavaVersion.current()}) is below the required JDK 25. Skipping AOT cache injection.",
                )
            } else {
                jvmArgs(
                    "-XX:AOTCache=${f.absolutePath}",
                    "-XX:+UseCompactObjectHeaders",
                    "-Dorg.jooq.no-logo=true",
                    "-XX:+EnableDynamicAgentLoading",
                )
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
    version.set(libs.versions.jooq.get())
    edition.set(JooqEdition.OSS)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"

                        properties.add(
                            Property().apply {
                                key = "scripts"
                                value = "${project.layout.buildDirectory.get()}/init.sql"
                            },
                        )
                    }
                    target.apply {
                        packageName = "com.example.codex.jooq"
                        directory = "src/main/generated"
                    }
                    generate.apply {
                        withDeprecated(false)
                        withImmutablePojos(true)
                        withFluentSetters(true)
                        withJpaAnnotations(true)
                        withImplicitJoinPathsAsKotlinProperties(true)
                        withKotlinSetterJvmNameAnnotationsOnIsPrefix(true)
                        withPojosAsKotlinDataClasses(true)
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

tasks.named("generateJooq").configure {
    dependsOn(generateInitSql)
    finalizedBy(lowercaseJooqNames)
    outputs.dir("src/main/generated")
}
val liquibaseRuntime = configurations.maybeCreate("liquibaseRuntime")

