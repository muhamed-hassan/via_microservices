FROM openjdk:11.0.7-slim
COPY ./target/currency_conversion_api-1.0.jar currency_conversion_api-1.0.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod","currency_conversion_api-1.0.jar"]