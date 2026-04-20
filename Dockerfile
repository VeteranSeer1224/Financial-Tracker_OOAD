FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q test package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/classes /app/classes

# Docker containers are typically headless, so this image runs the CLI app.
# Run JavaFX desktop UI on host machine with: mvn javafx:run
ENV APP_MAIN_CLASS=com.minip.financialtracker.App
CMD ["sh", "-c", "java -cp /app/classes ${APP_MAIN_CLASS}"]