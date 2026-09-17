FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY ai_customer_service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
