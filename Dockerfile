FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q test package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/classes /app/classes

CMD ["java", "-cp", "/app/classes", "com.minip.financialtracker.App"]