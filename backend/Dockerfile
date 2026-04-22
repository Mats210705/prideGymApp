# Multi-stage build para reducir tamaño final de imagen

# Stage 1: build con Maven + JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cacheo de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build del jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: runtime con solo JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render setea PORT dinamicamente
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
