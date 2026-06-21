#step 1 : Build the application using Maven
From maven:3.9.9-amazoncorretto-25 AS build
COPY . .
RUN mvn clean package -DskipTests

#step 2 : Run the application using OpenJdk
From amazoncorretto:25-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]