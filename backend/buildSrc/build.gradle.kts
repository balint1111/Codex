plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.testcontainers:testcontainers:2.0.5")
    implementation("org.postgresql:postgresql:42.7.4")
}
