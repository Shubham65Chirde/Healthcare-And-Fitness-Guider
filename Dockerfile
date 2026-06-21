# Use a single stable image that handles both build and run natively
FROM maven:3.9.9-sapmachine-25

# Set directory
WORKDIR /app
COPY . .

# Build inside the final container execution layer
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Command to run direct jar from target
CMD ["java", "-jar", "target/stayfit-backend-0.0.1-SNAPSHOT.jar"]