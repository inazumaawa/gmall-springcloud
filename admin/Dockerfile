FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY admin-1.0-SNAPSHOT.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
