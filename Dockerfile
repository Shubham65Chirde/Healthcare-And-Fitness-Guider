# Stage 1: Build using Sapmachine Java 25
FROM maven:3.9.9-sapmachine-25 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Explicitly force Sapmachine Java 25 as the ONLY runtime environment
FROM sapmachine:25-jre-headless-ubuntu
WORKDIR /app

# Copy the built jar from Stage 1 cleanly
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]