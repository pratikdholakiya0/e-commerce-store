# ----------------------------
# Stage 1: Build the Application
# ----------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only the POM first to cache dependencies
COPY pom.xml .
# Download dependencies (this speeds up future builds)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the JAR
# We skip tests to make the build faster on the server
RUN mvn clean package -DskipTests

# ----------------------------
# Stage 2: Run the Application
# ----------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the standard Spring Boot port
EXPOSE 8080

# OPTIMIZATION:
# 1. -Xmx350m: Limits memory to 350MB (Prevents crashing on free tier 512MB servers)
# 2. --enable-preview: Required because your pom.xml uses it
ENTRYPOINT ["java", "-Xmx350m", "-Xss512k", "-XX:TieredStopAtLevel=1", "--enable-preview", "-jar", "app.jar"]
