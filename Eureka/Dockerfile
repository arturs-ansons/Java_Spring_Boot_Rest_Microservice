FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built JAR from Maven target folder
COPY target/discovery-server-0.0.1-SNAPSHOT.jar app.jar

# Expose Eureka server port
EXPOSE 8761

# Run the Spring Boot JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
