FROM openjdk:18-jdk
EXPOSE 8080

COPY ./package/app.jar app.jar
ENTRYPOINT [ "java", "-jar", "/app.jar" ]