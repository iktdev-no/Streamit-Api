FROM openjdk:18-jdk
EXPOSE 8080

COPY ./package/api.jar api.jar
ENTRYPOINT [ "java", "-jar", "/api.jar" ] 