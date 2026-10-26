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

## Data

The service uses an in-memory H2 database, so data is reset on every restart. The schema is created
by Flyway migrations under `src/main/resources/db/migration`.

## API

| Method | Path                   | Purpose                                   |
|--------|------------------------|-------------------------------------------|
| POST   | `/api/offerings`       | Add an offering (name, price, active)     |
| PUT    | `/api/offerings/{id}`  | Change an offering                        |
| GET    | `/api/offerings/{id}`  | Get an offering                           |
| GET    | `/api/offerings`       | List offerings, paged                     |
| POST   | `/api/orders`          | Place an order; the server sets the total |
| GET    | `/api/orders/{id}`     | Get an order                              |

Every error is returned as `application/problem+json` (RFC 9457) with `type`, `title`, `status`,
`detail` and `instance`; validation errors add an `errors` list of `{field, message}`.

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
