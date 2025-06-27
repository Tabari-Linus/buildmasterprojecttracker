FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
LABEL authors="Mr Lii"

RUN groupadd -r mrlii && useradd -r -g mrlii mrlii

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN chown -R mrlii:mrlii /app
USER mrlii


# Expose port
EXPOSE 8080

EXPOSE 9010

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]