# Tasks: TS-018 — Shared-Library Best-Practice Remediation

> **ID:** `PROJ-9` · **Spec:** [../specs/PROJ-9-ts018.spec.md](../specs/PROJ-9-ts018.spec.md) · **Plan:** [../plans/PROJ-9-ts018.plan.md](../plans/PROJ-9-ts018.plan.md)
> Mỗi step: nhỏ nhất verify được → chạy test → 1 Conventional Commit. Trạng thái: ⬜ chưa · 🟨 đang · ✅ xong.
> **Verify nhanh:** `./mvnw test` (hoặc `-Dtest=<Class>`). **Verify phase:** `./mvnw clean verify -Ddependency-check.skip=true`.

## Bảng theo dõi

| Step | Phase (BC) | Gap | Nội dung | Test / Verify | Commit | TT |
|---|---|---|---|---|---|---|
| S0.1 | Setup | — | Checkout `feature/PROJ-9-ts018-shared-library-best-practices` từ `main` | `git status` sạch | — | ✅ |
| S0.2 | Setup | — | Baseline `./mvnw clean test` xanh trước khi sửa | 5 test class pass | — | ✅ |
| S1.1 | P1 Bootstrap | G1 | Thêm `spring-boot-autoconfigure` (`provided`) vào pom | `./mvnw -q dependency:resolve` ok | `build(deps): add spring-boot-autoconfigure (provided)` | ✅ |
| S1.2 | P1 Bootstrap | G1 | Tạo `CommonsAutoConfiguration` (`@AutoConfiguration @Import(GlobalExceptionHandler.class)`) | compile ok | (gộp S1.3) | ✅ |
| S1.3 | P1 Bootstrap | G1 | Tạo `META-INF/spring/....AutoConfiguration.imports` + `CommonsAutoConfigurationTest` (ContextRunner) | `-Dtest=CommonsAutoConfigurationTest` xanh (AC1) | `feat(autoconfigure): auto-register GlobalExceptionHandler` | ✅ |
| S2.1 | P1 Bootstrap | G3 | Gỡ `@Component` khỏi `TraceIdFilter` (giữ class + deprecation) | `./mvnw test` xanh (AC6) | `refactor(web): stop auto-registering deprecated TraceIdFilter` | ✅ |
| S3.1 | P2 Error-Contract | G4 | `@Order(Ordered.LOWEST_PRECEDENCE)` cho `GlobalExceptionHandler` | test advice cụ thể thắng (AC2) | `fix(exception): order GlobalExceptionHandler at LOWEST_PRECEDENCE` | ✅ |
| S4.1 | P2 Error-Contract | G5 | Thêm `tools.jackson:jackson-databind` (`provided`) | resolve ok | (gộp S4.2) | ✅ |
| S4.2 | P2 Error-Contract | G5 | Refactor parse dùng structured cause Jackson 3 (`getPath()`/`getTargetType()`); bỏ string-scraping; giữ `sanitize()` | post JSON sai kiểu → message đúng (AC3); `./mvnw test` xanh | `fix(exception): parse deserialization errors via structured Jackson causes` | ✅ |
| S5.1 | P2 Error-Contract | G6 | `GlobalExceptionHandlerContractTest` (standalone MockMvc, ma trận đầy đủ) | `-Dtest=GlobalExceptionHandlerContractTest` xanh (AC5) | `test(exception): add MockMvc contract tests for exception matrix` | ✅ |
| S6.1 | P3 Envelope | G8 | `package-info.java` `@NullMarked` cho api/exception/util/web/autoconfigure | `./mvnw clean verify -Ddependency-check.skip=true` (SpotBugs) sạch (AC7) | `docs(api): mark packages @NullMarked` | ✅ |
| S6.2 | P3 Envelope | G8 | Javadoc mô tả shape `ErrorDetail.details` (giữ `Object`) | javadoc build ok | (gộp S6.1) | ✅ |
| S7.1 | P3 Envelope | G2a | `ApiResult`: ICT → `ZoneOffset.UTC` mọi factory; cập nhật `ApiResultTest` assert UTC | `-Dtest=ApiResultTest` xanh (AC4) | `feat(api)!: stamp requestDateTime in UTC` + `BREAKING CHANGE:` | ✅ |
| S8.1 | Finalize | — | Full `./mvnw clean verify -Ddependency-check.skip=true` xanh | toàn bộ test + SpotBugs + javadoc | — | ✅ |
| S8.2 | Finalize | — | Cập nhật README (auto-config, UTC, TraceIdFilter) + tick checklist TS-018 | đọc lại | `docs: update README for auto-config, UTC and filter changes` | ✅ |
| S8.3 | Finalize | — | Push branch + mở PR tham chiếu TS-018 | CI + SonarCloud gate xanh (AC8) | — | ✅ |

## Definition of Done (mỗi step)
1. Code/thay đổi hoàn tất theo plan.
2. Có/chỉnh test tương ứng, chạy **xanh**.
3. `./mvnw test` (hoặc verify ở ranh giới phase) xanh.
4. 1 Conventional Commit rõ nghĩa; step breaking ghi `!` + `BREAKING CHANGE`.
5. Cập nhật trạng thái ở bảng trên.

## Ghi chú
- Thứ tự: non-breaking trước (P1, P2, G8), breaking G2a cuối.
- Điểm dễ sai: import Jackson 3 (`tools.jackson.databind.exc.*`) — verify bằng test thật trước khi tin.
