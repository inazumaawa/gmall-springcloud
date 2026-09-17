FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY favorites-1.0-SNAPSHOT.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
