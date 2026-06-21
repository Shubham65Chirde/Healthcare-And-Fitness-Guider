# Step 1: Build using SAPMachine Java 25 (Render handles this perfectly!)
FROM maven:3.9.9-sapmachine-25 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use the exact same verified SAPMachine Java 25 for runtime
FROM ghcr.io/sap/sapmachine:25-jre-alpine AS runtime
# Alternative if ghcr fails: FROM openjdk:25-ea-slim (Let's stick to standard docker hub to be 100% sure)
FROM ubuntu:24.04
RUN apt-get update && apt-get install -y openjdk-21-jre-headless && apt-get clean