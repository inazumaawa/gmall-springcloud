FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY customer_ws-1.0-SNAPSHOT.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java", "-jar", "app.jar"]
