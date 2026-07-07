# Plan: TS-018 — Shared-Library Best-Practice Remediation

> **ID:** `PROJ-9` · **Spec:** [docs/specs/PROJ-9-ts018.spec.md](../specs/PROJ-9-ts018.spec.md) · **Ngày:** 2026-07-07
> **Loại:** plan = **HOW**. Bám `spec.md`; quyết định lớn ghi ADR.

---

## 1. Chiến lược tổng thể

- **Branch:** `feature/PROJ-9-ts018-shared-library-best-practices` (pattern `feature/PROJ-N-...`, PROJ cao nhất hiện tại = 8), tách từ `main` mới nhất.
- **Cách chia:** Phase = **Bounded Context**. Mỗi gap = 1 step nhỏ verify được = 1 Conventional Commit. Gom non-breaking trước, breaking (G2a) cuối.
- **Điểm dừng:** commit + push + mở PR tham chiếu TS-018 → CI + SonarCloud gate.
- **Môi trường:** Java 21 / Spring Boot 4.0.6 / **Jackson 3 (`tools.jackson`)**. Test style: JUnit 5 + Mockito thủ công, assertions thuần JUnit (giữ nguyên).

## 2. Bounded Context → Phase

| Phase (Bounded Context) | Concern | Gap phủ | Breaking? |
|---|---|---|---|
| **P1 — Bootstrap & Portability** | Cách lib tự wire vào Spring container | G1 (auto-config), G3 (gỡ `@Component`) | G1 không · G3 nhẹ (de-register no-op) |
| **P2 — Error-Handling Contract** | Bề mặt lỗi API của handler | G4 (`@Order`), G5 (structured parse), G6 (contract test) | Không |
| **P3 — Response Envelope Contract** | Ngữ nghĩa DTO trả về | G2a (UTC), G8 (nullness + `ErrorDetail`) | G2a **có** (`feat!`) |

> **Vì sao gom vậy:** cùng chạm 1 vùng contract & cùng nhóm file lõi — P1 quanh cách đăng ký bean, P2 quanh `GlobalExceptionHandler`, P3 quanh `ApiResult`/`ErrorDetail`. Rollout & review theo vùng dễ suy luận, giảm xung đột.

## 3. Chi tiết từng Phase

### P1 — Bootstrap & Portability
- **G1 (R1):** thêm `spring-boot-autoconfigure` (`provided`) → tạo `vn/conghung/common/autoconfigure/CommonsAutoConfiguration.java` (`@AutoConfiguration @Import(GlobalExceptionHandler.class)`) → tạo resource `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Quyết định wiring: [ADR-001](../decisions/ADR-001-autoconfig-vs-componentscan.md).
- **G3 (R2):** [TraceIdFilter.java](../../src/main/java/vn/conghung/common/web/TraceIdFilter.java) — gỡ `@Component` (giữ class + `@Deprecated(forRemoval=true)`), note "no longer auto-registered".

### P2 — Error-Handling Contract
- **G4 (R3):** [GlobalExceptionHandler.java](../../src/main/java/vn/conghung/common/exception/GlobalExceptionHandler.java) — thêm `@Order(Ordered.LOWEST_PRECEDENCE)`.
- **G5 (R4):** thêm `tools.jackson:jackson-databind` (`provided`) → refactor `handleHttpMessageNotReadableException`: dùng `ex.getCause() instanceof tools.jackson.databind.exc.InvalidFormatException/MismatchedInputException`, lấy field từ `getPath()`, type từ `getTargetType()`; bỏ 3 helper string-scraping (`parseMismatchedInput`, `extractValueFromString`, phần substring trong `resolveDetailMessage`); giữ fallback text ngắn + `sanitize()`.
- **G6 (R5):** thêm `GlobalExceptionHandlerContractTest` — standalone `MockMvc` + controller nội bộ, phủ ma trận exception→status→responseCode→shape.

### P3 — Response Envelope Contract
- **G2a (R6):** [ApiResult.java](../../src/main/java/vn/conghung/common/api/ApiResult.java) — `ICT_ZONE (Asia/Ho_Chi_Minh)` → `ZoneOffset.UTC` trong mọi factory. Clock injection: **deferred** (factory static). Commit `feat!` + BREAKING CHANGE. Quyết định: [ADR-002](../decisions/ADR-002-ict-to-utc.md).
- **G8 (R7):** thêm `package-info.java` `@NullMarked` cho mỗi package (`api/exception/util/web/autoconfigure`); Javadoc `ErrorDetail.details` mô tả shape; giữ `Object` (đổi type sẽ breaking API).

## 4. Tái sử dụng (đừng viết lại)

- Factory `ApiResult.fail(code, desc, details)` — [ApiResult.java](../../src/main/java/vn/conghung/common/api/ApiResult.java) — dùng lại trong mọi handler.
- Record `ValidationError` — [ValidationError.java](../../src/main/java/vn/conghung/common/api/ValidationError.java) — shape của `ErrorDetail.details`.
- `sanitize()` chống log-injection — giữ nguyên khi refactor G5.
- jspecify `@NonNull` đã dùng ở `TraceIdFilter` → dùng `@NullMarked` cùng hệ.

## 5. Verify tổng thể (end-to-end)

- `CommonsAutoConfigurationTest` → AC1 (portable).
- `GlobalExceptionHandlerContractTest` (MockMvc thật) → AC2/AC3/AC5.
- `ApiResultTest` assert offset UTC → AC4.
- `./mvnw clean verify -Ddependency-check.skip=true` (SpotBugs + javadoc) → AC6/AC7 sạch.
- PR chạy full `mvn clean verify` + SonarCloud `qualitygate.wait=true` → AC8.

## 6. Ghi chú rủi ro

- **Jackson 3** là điểm dễ sai nhất: xác minh `getCause()` là `tools.jackson.databind.exc.*` bằng test post JSON sai kiểu thật trước khi tin import.
- **G2a breaking:** cần rollout đồng bộ service (ngoài repo này) — đã ghi BREAKING CHANGE.
