# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.9](https://github.com/C0ngHung/conghung-commons/compare/v0.2.8...v0.2.9) (2026-05-25)


### Features

* **exception:** add standardized spring mvc exception handlers for PROJ-6 ([#30](https://github.com/C0ngHung/conghung-commons/issues/30)) ([a0ad2f9](https://github.com/C0ngHung/conghung-commons/commit/a0ad2f94aa92f932b2fc5c7205b2970f63947ad8))

## [0.2.8](https://github.com/C0ngHung/conghung-commons/compare/v0.2.7...v0.2.8) (2026-05-25)


### Features

* **validation:** implement standardized validation errors list for PROJ-5 ([797d538](https://github.com/C0ngHung/conghung-commons/commit/797d538c3d1564197722a2275416b266f0f99697))

## [0.2.7](https://github.com/C0ngHung/conghung-commons/compare/v0.2.6...v0.2.7) (2026-05-25)


### Bug Fixes

* **api:** change timestamp to requestDateTime with OffsetDateTime timezone for PROJ-5 ([#24](https://github.com/C0ngHung/conghung-commons/issues/24)) ([d97e35c](https://github.com/C0ngHung/conghung-commons/commit/d97e35c31d86af32da254a56d913917e5112d5ac))

## [0.2.6](https://github.com/C0ngHung/conghung-commons/compare/v0.2.5...v0.2.6) (2026-05-23)


### Documentation

* **readme:** add snapshot vs release versioning explanation ([#20](https://github.com/C0ngHung/conghung-commons/issues/20)) ([34bdb5f](https://github.com/C0ngHung/conghung-commons/commit/34bdb5ff5e103e3f7c504e101453126878e16c48))

## [0.2.5](https://github.com/C0ngHung/conghung-commons/compare/v0.2.4...v0.2.5) (2026-05-22)


### Documentation

* update readme with correct api usage and version guide ([#17](https://github.com/C0ngHung/conghung-commons/issues/17)) ([d774037](https://github.com/C0ngHung/conghung-commons/commit/d774037e7c723732fdd129b14444c76eecf9664e))

## [0.2.4](https://github.com/C0ngHung/conghung-commons/compare/v0.2.3...v0.2.4) (2026-05-22)


### Bug Fixes

* **release:** integrate github app token to unblock CI ([#13](https://github.com/C0ngHung/conghung-commons/issues/13)) ([757839b](https://github.com/C0ngHung/conghung-commons/commit/757839ba800ad3275eaa3c28a4a85922ef393d6b))
* **release:** integrate github app token to unblock CI ([#14](https://github.com/C0ngHung/conghung-commons/issues/14)) ([1fcb8f2](https://github.com/C0ngHung/conghung-commons/commit/1fcb8f2574d920b2cad48be3e352be421ae98cfc))

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
