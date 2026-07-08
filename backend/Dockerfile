# Build the backend jar from the multi-module project (build context is the repo root).
FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY backend/pom.xml backend/pom.xml
COPY simulator/pom.xml simulator/pom.xml
COPY backend/src backend/src
RUN mvn -q -pl backend -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/backend/target/fleet-telemetry-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
