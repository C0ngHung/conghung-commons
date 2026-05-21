# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-05-21

### Added
- `ApiResult<T>` — Unified API response wrapper with `@JsonInclude(NON_NULL)`.
- `ErrorDetail` — Structured error detail record.
- `ResponseCode` — Generic infrastructure error code enum (0000 success, 0001-0009 validation, 0101-0102 auth, 4001 not found, 9999 internal error).
- `ApiException` — Abstract base exception binding `ResponseCode` to `HttpStatus`.
- `BusinessException` — Domain/business rule violation (HTTP 422).
- `TechnicalException` — Infrastructure failure (HTTP 503).
- `IntegrationException` — External system failure with `externalReference` and `correlationId` (HTTP 502).
- `UnknownResultException` — Timeout/unknown transaction result with `transactionReference`, `externalReference`, `correlationId` (HTTP 202).
- `ResourceNotFoundException` — Resource not found (HTTP 404).
- `GlobalExceptionHandler` — Centralized `@RestControllerAdvice` with tiered log levels.
- `TraceIdFilter` — `OncePerRequestFilter` that propagates `X-Trace-Id` header into SLF4J MDC.
