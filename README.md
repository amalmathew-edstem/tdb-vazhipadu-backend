# tdb-vazhipadu-backend

Backend service for booking vazhipadu (temple offerings) under the Travancore Devaswom Board (TDB).

## Stack

- Java 17
- Spring Boot 4 (Web MVC, Validation, Actuator)
- Maven, through the committed wrapper
- Lombok
- Spotless with Palantir Java Format

## Prerequisites

- JDK 17 or newer

## Getting started

```bash
./mvnw spring-boot:run
```

The service starts on port 8080. Health is reported at `GET /actuator/health`.

## Build and test

```bash
./mvnw -B spotless:check
./mvnw -B verify
```

Format the code before committing:

```bash
./mvnw spotless:apply
```

Run a single test:

```bash
./mvnw -B test -Dtest=ClassName#method
```

## Project layout

Code is packaged by feature, then by layer, under `com.edstem.tdbvazhipadu`:

```
com.edstem.tdbvazhipadu
├── TdbVazhipaduBackendApplication.java
├── config/
├── common/
└── <feature>/
    ├── controller/
    ├── service/
    ├── repository/
    ├── entity/
    └── dto/
```
