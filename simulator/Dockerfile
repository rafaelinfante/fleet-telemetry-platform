# Build the simulator jar from the multi-module project (build context is the repo root).
FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY backend/pom.xml backend/pom.xml
COPY simulator/pom.xml simulator/pom.xml
COPY simulator/src simulator/src
RUN mvn -q -pl simulator -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/simulator/target/fleet-device-simulator.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
