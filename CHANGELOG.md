# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.3](https://github.com/C0ngHung/conghung-commons/compare/v0.2.2...v0.2.3) (2026-05-22)


### Bug Fixes

* **ci:** solve local dependency-check failure and add devsecops guidelines (PROJ-4) ([#10](https://github.com/C0ngHung/conghung-commons/issues/10)) ([0764145](https://github.com/C0ngHung/conghung-commons/commit/07641455fb325eb1a6b63e4ae686589d15aedc9e))

## [0.2.2](https://github.com/C0ngHung/conghung-commons/compare/v0.2.1...v0.2.2) (2026-05-22)


### Bug Fixes

* **PROJ-4:** trigger release please with standard conventional commit format ([560e02d](https://github.com/C0ngHung/conghung-commons/commit/560e02d42a3edd5af387cb6779ed9656e76b4010))

## [0.2.0] - 2026-05-22

### Added
- Integrated SpotBugs v4.8.6.6 with FindSecBugs v1.13.0 for static security analysis (SAST).
- Integrated OWASP Dependency-Check v12.1.0 for software composition analysis (SCA) to check against NVD vulnerabilities.
- Added `suppression.xml` for false positive dependency vulnerability exclusion management.
- Configured SonarCloud properties in `pom.xml` for unified code quality and coverage reporting.
- Created `.github/workflows/ci.yml` for automated compilation, tests, DevSecOps analysis, and SonarCloud reporting on PRs.
- Created `.github/workflows/cd.yml` for automated versioning and deployment to GitHub Packages on tag push `v*`.

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
