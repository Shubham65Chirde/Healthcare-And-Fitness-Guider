#step 1 : Build the application using Maven
From maven:3.8.5-openjdk-17 AS build
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

#step 2 : Run the application using OpenJdk
From openjdk:17-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]