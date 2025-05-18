# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*jar-with-dependencies.jar app.jar

CMD ["java", "-jar", "app.jar"]
