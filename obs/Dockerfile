FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY obs-1.0-SNAPSHOT.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
