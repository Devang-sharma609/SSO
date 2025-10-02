# # Multi-stage build for optimized Spring Boot application
# # Stage 1: Build stage
# FROM maven:3.9-eclipse-temurin-17-alpine AS builder

# # Set working directory
# WORKDIR /app

# # Copy only pom.xml first to cache dependencies
# COPY pom.xml .

# # Download dependencies (cached layer if pom.xml doesn't change)
# RUN mvn dependency:go-offline -B

# # Copy source code
# COPY src ./src

# # Build the application (skip tests for faster builds)
# RUN mvn clean package -DskipTests -B

# # Extract layers from the JAR for better caching
# RUN mkdir -p target/dependency && \
#     cd target/dependency && \
#     jar -xf ../*.jar

# # Stage 2: Runtime stage with minimal JRE
# FROM eclipse-temurin:17-jre-alpine

# # Add metadata
# LABEL maintainer="devang-sharma609"
# LABEL description="SSO Authentication Service"
# LABEL version="0.1.3"

# # Create non-root user for security
# RUN addgroup -S spring && adduser -S spring -G spring

# # Set working directory
# WORKDIR /app

# # Copy dependencies from builder
# COPY --from=builder --chown=spring:spring /app/target/dependency/BOOT-INF/lib ./lib
# COPY --from=builder --chown=spring:spring /app/target/dependency/META-INF ./META-INF
# COPY --from=builder --chown=spring:spring /app/target/dependency/BOOT-INF/classes ./

# # Switch to non-root user
# USER spring:spring

# # Expose application port (default Spring Boot port)
# EXPOSE 8080

# # Health check
# HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
#     CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# # JVM optimization flags for containerized environments
# ENV JAVA_OPTS="-XX:+UseContainerSupport \
#     -XX:MaxRAMPercentage=75.0 \
#     -XX:InitialRAMPercentage=50.0 \
#     -XX:+ExitOnOutOfMemoryError \
#     -XX:+UseG1GC \
#     -XX:+UseStringDeduplication \
#     -Djava.security.egd=file:/dev/./urandom"

# # Run the application using exploded JAR format for faster startup
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp .:./lib/* com.devang.authentication.AuthenticationApplication"]

FROM maven:3.9.10-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# ===== Runtime Stage =====
FROM gcr.io/distroless/java17:nonroot
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/authentication-0.1.3.jar app.jar

# Expose only the app port
EXPOSE 8080

# Run as non-root (distroless nonroot is default)
USER nonroot

ENTRYPOINT ["java","-jar","app.jar"]
