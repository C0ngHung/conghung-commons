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

## 🛡️ DevSecOps & Contribution Guidelines

This repository enforces a strict, fully automated **DevSecOps Pipeline** and **Branch Protection Gate** to ensure that every release artifact is secure, highly performant, and clean.

### 1. PR Quality Gate & Security Check (CI)
Every Pull Request (PR) opened against `main` automatically triggers the **Continuous Integration (CI)** pipeline:
* **SAST (SpotBugs + FindSecBugs)**: Scans bytecode for security hotspots and critical bugs.
* **SCA (OWASP Dependency-Check)**: Audits all third-party libraries for open CVE vulnerabilities.
* **Code Quality (SonarCloud)**: Runs static analysis for maintainability, duplications, and coverage.

![GitHub Actions CI Workflow](image/CI-CD.png)
*Figure 1: GitHub Actions CI workflow performing automated audits and scans.*

* **Quality Gate Enforcer**: The pipeline **waits** for the SonarCloud Quality Gate results. If the scan fails the Quality Gate, the pipeline turns **RED** and GitHub's Branch Protection Rules will **block the PR from being merged**.

![SonarQube Quality Gate Rules](image/SonarQube Quality Gate.png)
*Figure 2: The standard "Sonar way" Quality Gate requirements enforced on all new code.*

#### 🟢 Passed (Safe to Merge) vs 🔴 Failed (Blocked) States
* When all quality gate conditions are met, the project health reports green and the PR is ready to merge:
  
  ![SonarQube Health Passed](image/SonarQube Success.png)
  *Figure 3: Clean project health dashboard on SonarCloud (Passed).*

* If any check fails, the gate blocks the pipeline immediately, protecting the main codebase:

  ![SonarCloud Quality Gate Failed](image/SonarCloud Fail.png)
  *Figure 4: Automated block triggered by low test coverage or security vulnerabilities (Failed).*

### 2. Conventional Commits Discipline (Release Automation)
We use `release-please` to automatically generate semantic version bumps and update the `CHANGELOG.md` upon merging PRs to `main`.
* **PR Title Format**: Your PR title **MUST** follow [Conventional Commits](https://www.conventionalcommits.org/):
  ```text
  <type>(<scope>): <short description>
  ```
  *Examples*: `feat(api): add new validation filters`, `fix(security): sanitize exception logging params`
* ⚠️ **Strict Syntax Warning**: Use standard **parentheses `()`** for the scope. Using **brackets `[]`** (e.g. `feat[api]: ...`) will fail to be parsed by the automation bot, preventing automated release PRs from being generated.

![SonarCloud Scan History](image/History.png)
*Figure 5: Historical record of analyzed pull requests and branches on SonarCloud.*

### 3. How to Inspect Quality Gate Failures (For Contributors)
If your PR fails the Quality Gate and is blocked from merging, don't worry! Follow these steps to find and fix the issues:
1. **Check the PR Comments**: SonarCloud automatically decorates your PR with a summary comment. Click on the link **"See analysis details on SonarCloud"** inside that comment.
2. **Access the Public Dashboard**: 
   * Go directly to our public [conghung-commons SonarCloud Dashboard](https://sonarcloud.io/summary/new_code?id=C0ngHung_conghung-commons).
   * Click **"Log in"** at the top right of the page and select **"With GitHub"** (no special permissions are requested, as our repository is **100% public**).
3. **Find your Branch / PR**:
   * On the dashboard, click on the **"Pull Requests"** tab to see the active analysis of all pull requests.
   * Click on your specific branch name to open its dedicated report.
4. **Pinpoint the Issues**:
   * Navigate to the **"Issues"** tab on the report dashboard to see the exact line of code, the rule violated (e.g. Code Smell, Bug, or Vulnerability), and a detailed explanation of how to fix it.
   * Go to the **"Security Hotspots"** tab to review and resolve sensitive code patterns.
5. **Local Prevention (Recommended)**: To catch these issues before pushing to GitHub, install the **SonarLint** extension in your IDE (IntelliJ IDEA, VS Code, Eclipse) and bind it to our SonarCloud project (`C0ngHung_conghung-commons`). It will give you instant, real-time feedback as you write code!

## License

[MIT](LICENSE)
