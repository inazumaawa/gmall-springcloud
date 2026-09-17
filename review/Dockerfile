FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY review-1.0-SNAPSHOT.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
