# Release Tracker API

A production-ready REST API for tracking software releases through their lifecycle, built with Spring Boot 3, PostgreSQL, and Liquibase.

## Tech Stack

| Layer            | Technology                       |
|------------------|----------------------------------|
| Language         | Java 21+                         |
| Framework        | Spring Boot 3.4.4                |
| Build Tool       | Gradle 9.4                       |
| Database         | PostgreSQL 16                    |
| Migrations       | Liquibase                        |
| ORM              | Spring Data JPA / Hibernate      |
| API Docs         | Springdoc OpenAPI (Swagger UI)   |
| Mapping          | MapStruct                        |
| Containerization | Docker + Docker Compose          |
| Testing          | JUnit 5, Testcontainers, MockMvc |

## Quick Start

### Using Docker Compose (recommended)

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`.

### Local Development

**Prerequisites:** Java 21+, PostgreSQL running on port 5432

```bash
# Start PostgreSQL (or use docker compose for just the DB)
docker compose up db -d

# Run the application
./gradlew bootRun
```

## API Documentation

Once the application is running, access the Swagger UI:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI spec:** http://localhost:8080/api-docs

## API Endpoints

| Method   | Endpoint                  | Description                          |
|----------|---------------------------|--------------------------------------|
| `GET`    | `/api/v1/releases`        | List releases (paginated, filterable)|
| `GET`    | `/api/v1/releases/{id}`   | Get a single release                 |
| `POST`   | `/api/v1/releases`        | Create a new release                 |
| `PUT`    | `/api/v1/releases/{id}`   | Update a release                     |
| `DELETE` | `/api/v1/releases/{id}`   | Delete a release                     |

### Filter & Pagination Parameters (GET /api/v1/releases)

| Parameter        | Type   | Description                           |
|------------------|--------|---------------------------------------|
| `name`           | string | Partial name match (case-insensitive) |
| `status`         | string | Exact status match                    |
| `releaseDateFrom`| date   | Filter from this date (inclusive)     |
| `releaseDateTo`  | date   | Filter to this date (inclusive)       |
| `page`           | int    | Page number (0-indexed, default: 0)   |
| `size`           | int    | Page size (default: 20)               |
| `sort`           | string | Sort field and direction (e.g., `name,asc`) |

### Release Statuses

| Status              |
|---------------------|
| Created             |
| In Development      |
| On DEV              |
| QA Done on DEV      |
| On Staging          |
| QA Done on STAGING  |
| On PROD             |
| Done                |

### Example Requests

**Create a release:**
```bash
curl -X POST http://localhost:8080/api/v1/releases \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "v2.1.0",
    "description": "Feature release with auth improvements",
    "releaseDate": "2026-05-01"
  }'
```

**List releases with filters:**
```bash
curl "http://localhost:8080/api/v1/releases?status=In%20Development&page=0&size=10"
```

**Update a release:**
```bash
curl -X PUT http://localhost:8080/api/v1/releases/{id} \
  -H 'Content-Type: application/json' \
  -d '{
    "status": "On DEV",
    "description": "Deployed to dev environment"
  }'
```

**Delete a release:**
```bash
curl -X DELETE http://localhost:8080/api/v1/releases/{id}
```

## Running Tests

```bash
# Unit tests only
./gradlew test

# Integration tests only (requires Docker for Testcontainers)
./gradlew integrationTest

# All tests
./gradlew check
```

## Project Structure

```
src/main/java/com/releasetracker/
├── config/              # OpenAPI and Jackson configuration
├── controller/          # REST controllers
├── dto/                 # Request/response DTOs (Java records)
├── entity/              # JPA entities
├── enums/               # Status enumerations
├── exception/           # Custom exceptions and global error handler (RFC 7807)
├── mapper/              # MapStruct mappers
├── repository/          # Spring Data JPA repositories
├── service/             # Business logic layer
└── specification/       # JPA Specifications for dynamic query filters

src/main/resources/
├── application.yml                      # Application configuration (with docker profile)
└── db/changelog/                        # Liquibase migrations
    ├── db.changelog-master.yaml
    └── 001-create-releases-table.yaml
```

## Architecture Highlights

- **RFC 7807 Problem Details** for standardized error responses
- **JPA Specifications** for type-safe, composable query filters
- **MapStruct** for compile-time DTO mapping (zero reflection overhead)
- **Liquibase** for versioned, repeatable database migrations
- **Multi-stage Docker build** for minimal production image size
- **Testcontainers** for integration tests against real PostgreSQL
- **Pagination & sorting** with Spring Data's `Pageable`
- **Non-root Docker user** for container security
- **Health checks** via Docker HEALTHCHECK + Spring Actuator
- **API versioning** via URL path (`/api/v1/`)
- **Java records** for immutable DTOs
- **Separate unit and integration test tasks** via Gradle

## Health & Monitoring

- **Health:** http://localhost:8080/actuator/health
- **Info:** http://localhost:8080/actuator/info
- **Metrics:** http://localhost:8080/actuator/metrics
