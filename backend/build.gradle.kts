import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.Property
plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.1.21"
    id("org.springframework.boot") version "3.1.1"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.liquibase.gradle") version "2.2.1"
    id("nu.studer.jooq") version "8.2"
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

    implementation("org.jooq:jooq:3.19.22")
    runtimeOnly("org.postgresql:postgresql")
    jooqGenerator("org.postgresql:postgresql")
    jooqGenerator("org.liquibase:liquibase-core")
    jooqGenerator("org.jooq:jooq-meta-extensions-liquibase:3.19.22")
    implementation("org.liquibase:liquibase-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jooq {
    version.set("3.19.22")
    edition.set(JooqEdition.OSS)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    strategy.name = "org.jooq.codegen.KeepNamesGeneratorStrategy"
                    database.apply {
                        name = "org.jooq.meta.extensions.liquibase.LiquibaseDatabase"
                        properties.add(Property().apply {
                            key = "rootPath"
                            value = "${projectDir}/src/main/resources"
                        })
						properties.add(Property().apply {
                            key = "scripts"
                            value = "db/changelog/db.changelog-master.yaml"
                        })
                    }
                    target.apply {
                        packageName = "com.example.codex.jooq"
                        directory = "src/main/generated"
                    }
                }
            }
        }
    }
}




tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.register("lowercaseJooqNames") {
    doLast {
        println("Post-processing jOOQ sources to lowercase names")
        fileTree("src/main/generated") { include("**/*.kt") }.forEach { file ->
            exec {
                commandLine("perl", "scripts/lowercase.pl", file.absolutePath)
            }
        }
    }
}

tasks.named("generateJooq").configure {
    finalizedBy("lowercaseJooqNames")
}
