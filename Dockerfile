# -------- Stage 1: Build the JAR --------
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy all project files
COPY . .

# Build the JAR
RUN mvn clean package -DskipTests

# -------- Stage 2: Run the JAR --------
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (optional, for documentation)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
