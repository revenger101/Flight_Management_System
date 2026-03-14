# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Cache dependencies first for faster rebuilds
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Build application
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Use non-root user for runtime safety
RUN addgroup --system app && adduser --system --ingroup app app

# Copy all built artifacts, then select the Spring Boot fat jar
COPY --from=builder /app/target /app/target
RUN set -eux; \
    JAR_PATH="$(find /app/target -maxdepth 1 -type f -name '*.jar' ! -name '*original*' | head -n 1)"; \
    cp "$JAR_PATH" /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

USER app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
