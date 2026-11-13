# Stage 1: Build Application
FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /workspace

# Copy Maven Wrapper and POM first to leverage Docker layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy application source code and compile jar
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Runtime Image
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Run as non-root user for production security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

COPY --from=builder /workspace/target/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
