# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies (cached layer) - use Maven directly
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install wget for healthcheck
RUN apk add --no-cache wget curl

WORKDIR /app

# Create user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy unpacked JAR layers for better caching
ARG DEPENDENCY=/build/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Change ownership
RUN chown -R spring:spring /app

USER spring:spring

EXPOSE 8080

# Java 21 optimized JVM options
ENV JAVA_OPTS="\
    -XX:+UseZGC \
    -XX:+ZGenerational \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=60.0 \
    -XX:InitialRAMPercentage=40.0 \
    -XX:MetaspaceSize=256m \
    -XX:MaxMetaspaceSize=512m \
    -XX:+OptimizeStringConcat \
    -XX:+UseStringDeduplication"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp /app:/app/lib/* com.obuspartners.ObusPartnersApiApplication"]

