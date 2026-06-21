# Stage 1: Build the application using verified Maven + Sapmachine Java 25
FROM maven:3.9.9-sapmachine-25 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Safe, light-weight Ubuntu production runtime environment
FROM ubuntu:24.04
RUN apt-get update && apt-get install -y openjdk-21-jre-headless && apt-get clean

# Copy the built jar from Stage 1
COPY --from=build /target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]