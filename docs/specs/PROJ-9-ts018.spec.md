# Spec: TS-018 — Shared-Library Best-Practice Remediation

> **ID:** `PROJ-9` · **Nguồn (SRS):** [troubleshooting/TS-018-commons-shared-library-best-practice-audit.md](../../troubleshooting/TS-018-commons-shared-library-best-practice-audit.md) · **Ngày:** 2026-07-07
> **Trạng thái:** ✅ APPROVED · **Người duyệt:** dev repo commons
> **Loại:** spec = **WHAT + WHY**. HOW ở [plan](../plans/PROJ-9-ts018.plan.md).

---

## 1. Vấn đề & Mục tiêu (Why)

- **Bối cảnh / nỗi đau:** Audit TS-018 chấm `conghung-commons` v0.3.0 ~7.0/10 về tiêu chí "shared library". Điểm trừ chính: lib **thiếu Spring Boot auto-configuration**, nên `GlobalExceptionHandler` chỉ load được nhờ mọi consumer tình cờ đặt package root `vn.conghung`. Một service ở package khác sẽ **âm thầm mất handler** → rơi về HTTP 500 (đúng sự cố TS-001). Kèm theo là các gap về thứ tự advice, parse lỗi dễ vỡ, thiếu contract test, lệch timezone, nullness không nhất quán.
- **Mục tiêu:** Đưa `conghung-commons` thành thư viện **portable đúng chuẩn starter**, có **contract test** cho bề mặt lỗi, **nhất quán timezone/nullness** — để mọi microservice dùng plug-and-play, không còn coupling ẩn.
- **Non-goals:** Không đụng repo microservice (G7 version tập trung, G2b handler UTC). Không redesign `ApiResult` sang bean có state.

## 2. Phạm vi (Scope)

| Trong scope (repo commons) | Ngoài scope |
|---|---|
| G1 auto-config · G3 gỡ `@Component` · G4 `@Order` · G5 structured parse · G6 contract test · G8 nullness/`ErrorDetail` · G2a timezone→UTC | G7 (version tập trung ở parent pom microservice) · G2b (các `*ExceptionHandler` service đi qua factory) — thuộc repo microservice |

## 3. Yêu cầu (What) — truy vết theo gap audit

| # | Yêu cầu | Ưu tiên | Nguồn |
|---|---|---|---|
| R1 | Lib tự cấu hình `GlobalExceptionHandler` qua auto-config, không phụ thuộc package root của consumer | 🔴 Cao | G1 |
| R2 | Filter no-op `TraceIdFilter` không còn tự đăng ký qua stereotype-scan | 🟠 TB | G3 |
| R3 | Advice global có thứ tự tường minh, không che advice cụ thể của service | 🟠 TB | G4 |
| R4 | Parse lỗi deserialization bằng structured cause, ổn định qua version | 🟡 Thấp-TB | G5 |
| R5 | Có contract test cho ma trận exception → HTTP status + responseCode + shape | 🟡 Thấp | G6 |
| R6 | `requestDateTime` nhất quán UTC trên mọi factory | 🟠 TB | G2a |
| R7 | Nullness nhất quán toàn module; document rõ shape của `ErrorDetail.details` | 🟡 Thấp | G8 |

## 4. Acceptance Criteria (nghiệm thu — bám mục 6 audit)

- [ ] **AC1 (R1):** Service đặt ở package bất kỳ (vd `com.test`) vẫn nhận `GlobalExceptionHandler` **chỉ nhờ có dependency** — chứng minh bằng `ApplicationContextRunner` load auto-config, không component-scan.
- [ ] **AC2 (R3):** Advice cụ thể của service (vd bắt `BusinessException`) **luôn thắng** catch-all global sau khi thêm `@Order(LOWEST_PRECEDENCE)`.
- [ ] **AC3 (R4):** JSON malformed / sai kiểu vẫn ra message hợp lý (field + type) sau khi bỏ string-scraping.
- [ ] **AC4 (R6):** `requestDateTime` có offset **UTC** ở cả success lẫn error.
- [ ] **AC5 (R5):** Contract test phủ đủ: Technical→503, Integration→502, UnknownResult→202, Business→422, validation→400, type-mismatch→400, method-not-supported→405, constraint→400, generic→500.
- [ ] **AC6 (R2):** `TraceIdFilter` không còn `@Component`; không xuất hiện trong filter chain của app.
- [ ] **AC7 (R7):** Module `@NullMarked`; Javadoc `ErrorDetail.details` mô tả các shape (thường `List<ValidationError>`).
- [ ] **AC8 (chung):** `mvn clean verify` + SonarCloud quality gate PASS trên PR.

## 5. Ràng buộc & Giả định

- **Ràng buộc:** Java 21, Spring Boot parent 4.0.6, publish GitHub Packages, release-please + Conventional Commits. G2a là **breaking** (client parse local time sẽ lệch) → commit `feat!`, ghi changelog.
- **Giả định (cần verify khi làm):**
  - Spring Boot 4 message converter dùng **Jackson 3** (`tools.jackson.databind.exc.*`) → `HttpMessageNotReadableException.getCause()` là exception Jackson 3. *(Đã verify sơ bộ: `jackson-bom.version=3.1.2` trong spring-boot-dependencies 4.0.6.)*
  - `spring-boot-autoconfigure` và `jspecify` có sẵn để dùng ở scope `provided`. *(Đã verify: có 4.0.6 và jspecify 1.0.0 trong m2.)*

## 6. Rủi ro nổi bật (chi tiết ở ADR)

- Wiring handler: auto-config vs component-scan → [ADR-001](../decisions/ADR-001-autoconfig-vs-componentscan.md).
- Đổi timezone ICT→UTC (breaking) → [ADR-002](../decisions/ADR-002-ict-to-utc.md).
- Jackson 3 package đổi (`tools.jackson`) — điểm dễ sai nhất của G5.

## 7. Liên kết

- Plan: [docs/plans/PROJ-9-ts018.plan.md](../plans/PROJ-9-ts018.plan.md)
- Tasks: [docs/tasks/PROJ-9-ts018.tasks.md](../tasks/PROJ-9-ts018.tasks.md)
- Decisions: [ADR-001](../decisions/ADR-001-autoconfig-vs-componentscan.md) · [ADR-002](../decisions/ADR-002-ict-to-utc.md)
