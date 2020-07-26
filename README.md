# Employee API

Offerring various endpoints to manage employees data along with some currency conversion services.

## Prerequisites:

* JDK 11 or higher
* Docker engine, and docker compose
* Any IDE that supports Java

#### How to run:

`./mvnw.cmd clean install` with windows OS, or using `./mvnw clean install` with Unix based OS will build the API and run it's automated tests (UTs, and ITs)

`docker-compose up` will run 2 services together {PostgreSQL DB, and the API}


#### How to use API DOCS via Swagger:

http://localhost:8081/swagger-ui.html



