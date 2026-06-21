# Stage 1: Build using official stable Maven image
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Hard-lock runtime to exact Sapmachine Java 25
FROM sapmachine:25-jre-headless-ubuntu
WORKDIR /app

# Copy the built jar from Stage 1 cleanly
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]