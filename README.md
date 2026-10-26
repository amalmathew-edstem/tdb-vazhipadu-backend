# tdb-vazhipadu-backend

Backend service for booking vazhipadu (temple offerings) under the Travancore Devaswom Board (TDB).

## Stack

- Java 17
- Spring Boot 4 (Web MVC, Validation, Actuator, Security with JWT bearer tokens)
- Maven, through the committed wrapper
- Lombok
- Spotless with Palantir Java Format

## Prerequisites

- JDK 17 or newer

## Getting started

The service needs a JWT signing key and a staff account from the environment. It refuses to start
without them, and none of them is ever committed.

| Variable                  | Meaning                                                  |
|---------------------------|----------------------------------------------------------|
| `TDB_JWT_SECRET`          | HMAC signing key for staff tokens, at least 32 characters |
| `TDB_STAFF_USERNAME`      | Staff login name                                         |
| `TDB_STAFF_PASSWORD_HASH` | BCrypt hash of the staff password                        |

```bash
export TDB_JWT_SECRET="$(openssl rand -base64 48)"
export TDB_STAFF_USERNAME=counter
export TDB_STAFF_PASSWORD_HASH="$(htpasswd -bnBC 10 "" '<password>' | tr -d ':\n')"
./mvnw spring-boot:run
```

The service starts on port 8080. Health is reported at `GET /actuator/health`.

## Staff access

Order search and offering management are for temple staff only. Staff sign in with
`POST /api/auth/login` and send the returned token on each request as
`Authorization: Bearer <accessToken>`. Tokens expire after 8 hours.

```bash
curl -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"counter","password":"<password>"}'
```

## Data

The service uses an in-memory H2 database, so data is reset on every restart. The schema is created
by Flyway migrations under `src/main/resources/db/migration`.

## API

| Method | Path                  | Access | Purpose                                                            |
|--------|-----------------------|--------|--------------------------------------------------------------------|
| POST   | `/api/auth/login`     | Public | Staff sign-in; returns a bearer token                              |
| POST   | `/api/offerings`      | Staff  | Add an offering (type, price, active)                              |
| PUT    | `/api/offerings/{id}` | Staff  | Change an offering                                                 |
| GET    | `/api/offerings/{id}` | Public | Get an offering                                                    |
| GET    | `/api/offerings`      | Public | List offerings, paged                                              |
| POST   | `/api/orders`         | Public | Place an order; the server sets the total                          |
| GET    | `/api/orders`         | Staff  | Search orders by `offeringDate`, `status` and `phoneNumber`, paged |
| GET    | `/api/orders/{id}`    | Public | Get an order                                                       |

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
