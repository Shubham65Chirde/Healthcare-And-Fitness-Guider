# Step 1: Use the exact multi-architecture stable Maven + JDK 25 verified repository
FROM maven:3.9.9-sapmachine-25 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use stable runtime execution environment
FROM ghcr.io/graalvm/jdk:ol9-java25
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]