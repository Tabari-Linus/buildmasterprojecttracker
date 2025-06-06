FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN chown lii:lii app.jar

USER lii

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]