FROM openjdk:11.0.7-slim
RUN apt-get -y update && apt-get -y install curl
HEALTHCHECK --interval=5s CMD curl -f http://localhost:1111/actuator/health || exit 1
COPY ./target/employee_api-1.0.jar employee_api-1.0.jar
EXPOSE 1111
