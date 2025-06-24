FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY *.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
