FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY target/account-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]