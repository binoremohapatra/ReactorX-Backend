# --- Stage 1: Build the application using Maven ---
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set the working directory
WORKDIR /app

# Copy pom.xml and dependencies build steps
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Package the application
RUN mvn package -DskipTests

# --- Stage 2: Deploy on the matching stable JRE environment ---
FROM eclipse-temurin:17-jre

# Set the working directory
WORKDIR /app

# Copy the packaged JAR file
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application when the container starts
ENTRYPOINT ["java","-jar","/app/app.jar"]