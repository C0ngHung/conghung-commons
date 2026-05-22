# conghung-commons

[![Continuous Integration](https://github.com/c0nghung/conghung-commons/actions/workflows/ci.yml/badge.svg)](https://github.com/c0nghung/conghung-commons/actions/workflows/ci.yml)
[![Continuous Delivery](https://github.com/c0nghung/conghung-commons/actions/workflows/cd.yml/badge.svg)](https://github.com/c0nghung/conghung-commons/actions/workflows/cd.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=C0ngHung_conghung-commons&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=C0ngHung_conghung-commons)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=C0ngHung_conghung-commons&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=C0ngHung_conghung-commons)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=C0ngHung_conghung-commons&metric=bugs)](https://sonarcloud.io/summary/new_code?id=C0ngHung_conghung-commons)

Lightweight shared infrastructure kernel for Spring Boot web applications.

## What's Included

| Package | Class | Purpose |
|---------|-------|---------|
| `api` | `ApiResult<T>` | Unified API response wrapper with `code`, `message`, `data`, `error`, `traceId`, `timestamp` |
| `api` | `ErrorDetail` | Structured error detail with `code`, `message`, `details` |
| `exception` | `ResponseCode` | Stable numeric error code enum for generic infrastructure concerns |
| `exception` | `ApiException` | Abstract base exception with `ResponseCode` + `HttpStatus` |
| `exception` | `BusinessException` | Domain/business rule violation (HTTP 422) |
| `exception` | `TechnicalException` | Infrastructure failure (HTTP 503) |
| `exception` | `IntegrationException` | External system failure (HTTP 502) |
| `exception` | `UnknownResultException` | Timeout/unknown transaction result (HTTP 202) |
| `exception` | `ResourceNotFoundException` | Resource not found (HTTP 404) |
| `exception` | `GlobalExceptionHandler` | Centralized `@RestControllerAdvice` with proper log levels |
| `web` | `TraceIdFilter` | SLF4J MDC-based `traceId` propagation via `X-Trace-Id` header |

## Error Code Ranges

This library provides **generic infrastructure codes only**. Domain-specific codes should be defined in each microservice.

| Code | Enum | Category |
|------|------|----------|
| `0000` | `COMMON_SUCCESS` | Success |
| `1001` | `REQ_VALIDATION_ERROR` | Invalid input data |
| `1002` | `REQ_BAD_REQUEST` | Bad request |
| `2001` | `AUTH_UNAUTHORIZED` | Unauthorized |
| `2101` | `PERM_FORBIDDEN` | Forbidden |
| `4001` | `DATA_NOT_FOUND` | Resource not found |
| `4002` | `DATA_CONFLICT` | Conflict request |
| `9999` | `SYS_INTERNAL_ERROR` | Internal server error |

## Requirements

- Java 21+
- Spring Boot 4.x (Spring Framework 7.x)

## Installation

### Maven (GitHub Packages)

Add the repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/c0nghung/conghung-commons</url>
    </repository>
</repositories>
```

Add the dependency:

```xml
<dependency>
    <groupId>vn.conghung</groupId>
    <artifactId>conghung-commons</artifactId>
    <version>0.1.0</version>
</dependency>
```

Configure authentication in `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_PAT</password>
    </server>
</servers>
```

## Design Principles

- **Minimal footprint** — No Spring Boot starters, no embedded Tomcat, no Lombok.
- **Infrastructure only** — No business logic, no domain-specific code.
- **Immutable DTOs** — All response objects use Java `record` types.
- **Extensible error codes** — Each microservice defines its own domain-specific `ResponseCode`.
- **Traceability** — Every API response includes a `traceId` synchronized with SLF4J MDC.

## License

[MIT](LICENSE)
