# Stage 1 — build all modules with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy all pom files first so Maven can cache dependencies
COPY pom.xml .
COPY api-gateway/pom.xml          api-gateway/
COPY user-service/pom.xml         user-service/
COPY product-service/pom.xml      product-service/
COPY order-service/pom.xml        order-service/
COPY payment-service/pom.xml      payment-service/
COPY notification-service/pom.xml notification-service/
RUN mvn dependency:go-offline -q

# Copy sources and build all modules
COPY . .
RUN mvn package -DskipTests -q

# Stage 2 — minimal runtime image, copy only the target service jar
FROM eclipse-temurin:21-jre-jammy
ARG SERVICE_NAME
COPY --from=build /app/${SERVICE_NAME}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
