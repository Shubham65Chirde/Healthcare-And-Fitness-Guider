#step 1 : Build the application using Maven
From maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

#step 2 : Run the application using OpenJdk
From eclipse-temurin:17-jdk-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]