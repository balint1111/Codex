# Stage 1: build
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy only the wrapper + Gradle settings so Docker can cache layers
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/
RUN chmod +x gradlew

# normalize line endings and make executable
RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew

# Copy source & build
COPY src/ src/
RUN ./gradlew bootJar --no-daemon

# Stage 2: runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
