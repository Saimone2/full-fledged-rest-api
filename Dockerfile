FROM openjdk:17-alpine
MAINTAINER sa1mone
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]