FROM openjdk:11.0.7-slim
COPY ./target/employee_api-1.0.jar employee_api-1.0.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "employee_api-1.0.jar"]