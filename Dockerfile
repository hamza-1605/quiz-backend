# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
# Windows line endings would break the Maven wrapper script on Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY src src
RUN ./mvnw -B package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/target/*.jar app.jar
USER app

# Tuned for a 512 MB container with very little CPU
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]