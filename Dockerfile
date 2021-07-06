FROM openjdk:18-jdk-buster
EXPOSE 8080

COPY ./package/api.jar api.jar
ENTRYPOINT [ "java", "-jar", "/api.jar" ] 