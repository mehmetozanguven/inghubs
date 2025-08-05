# Use an official Java 21 base image
FROM eclipse-temurin:21-jre-ubi9-minimal

LABEL maintainer="mehmetozanguven"
LABEL version="1.0.0"

RUN useradd -ms /bin/bash appuser

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

# Run the app
ENTRYPOINT ["java","-jar","/app/app.jar"]
