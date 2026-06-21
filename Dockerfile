# Step 1: Build Phase using Sapmachine Java 25
FROM maven:3.9.9-sapmachine-25 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Continuous Runtime using exact same Sapmachine Java 25
FROM sapmachine:25-jre-headless-ubuntu
COPY --from=build /target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]