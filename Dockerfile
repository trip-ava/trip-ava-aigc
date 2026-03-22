# Multi-stage build for AVA Backend
# Stage 1: Build
FROM maven:3.8.6-openjdk-17-slim AS build

WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:17-slim

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user for security
RUN groupadd -r ava && useradd -r -g ava ava
RUN chown -R ava:ava /app
USER ava

# Expose port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/ava/hello || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
