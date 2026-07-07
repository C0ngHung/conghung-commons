# [AUDIT] `conghung-commons` v0.3.0 — Đánh giá best-practice cho Shared Library

> **Tags:** `shared-library` `spring-boot` `auto-configuration` `api-contract` `exception-handling` `best-practice` `microservices`
> **Áp dụng cho:** `vn.conghung:conghung-commons:0.3.0` (repo riêng `c0nghung/conghung-commons`, publish GitHub Packages)
> **Ngày ghi nhận:** 2026-07-07
> **Trạng thái:** 📝 AUDIT — doc-only, **chưa sửa code** ở cả repo commons lẫn repo này
> **Nối tiếp:** TS-001 (handler không load), TS-004 (commons improvement proposals), TS-008 (pagination/sort commons)

---

## 1. Phạm vi & phương pháp

Đánh giá xem thư viện dùng chung `conghung-commons` đã đạt **best practice cho việc share giữa các
microservice** hay chưa. Source của lib **không nằm trong repo này** — được đọc từ
`-sources.jar` trong `~/.m2/repository/vn/conghung/conghung-commons/0.3.0/`.

**Đã đọc step-by-step toàn bộ 18 file** của bản 0.3.0:

| Package | File |
|---|---|
| `api/` | `ApiResult`, `ResultInfo`, `ErrorDetail`, `PageResponse`, `ValidationError` |
| `exception/` | `ApiException`, `BusinessException`, `ResourceNotFoundException`, `IntegrationException`, `TechnicalException`, `UnknownResultException`, `ResponseCode`, `GlobalExceptionHandler` |
| `util/` | `PageableFactory`, `SortParser` |
| `web/` | `TraceIdFilter` |
| gốc | `pom.xml` |

**Đã trace cách 6 service consume lib** (order / product / account / mail / auth / gateway):
- Tất cả main class đặt ở `package vn.conghung` → component-scan tình cờ với tới `vn.conghung.common.*`.
- 4 service pin `conghung-commons.version = 0.3.0` **riêng lẻ** trong từng pom.
- 3 service tự khai `@RestControllerAdvice` riêng (`OrderExceptionHandler`, `ProductExceptionHandler`, `AccountExceptionHandler`).
- **Verify nội dung jar biên dịch:** `META-INF/` chỉ có `MANIFEST.MF` + maven descriptors → **KHÔNG có** `AutoConfiguration.imports` / `spring.factories`.

---

## 2. Kết luận (Verdict)

> **Chất lượng code lõi cao, nhưng CHƯA đạt best-practice cho một "shared library" đúng nghĩa.**
> Điểm trừ chính: **thiếu Spring Boot auto-configuration** → lib phụ thuộc ngầm vào việc mọi
> consumer trùng package root `vn.conghung`. Đây là coupling ẩn, dễ gây regression im lặng.

**Điểm: ~7.0 / 10** cho tiêu chí *shared-library best practice*.

| Nhóm | Số lượng | Ghi chú |
|---|---|---|
| 🔴 Cao | 1 | G1 — thiếu auto-configuration (tính portable) |
| 🟠 Trung bình | 3 | G2 timezone/bypass factory · G3 `@Component` filter · G4 thứ tự advice |
| 🟡 Thấp | 4 | G5 parse message · G6 test · G7 version tập trung · G8 `ErrorDetail`/nullness |

| Fix ở repo nào | Gap |
|---|---|
| **commons** (`c0nghung/conghung-commons`) | G1, G3, G4, G5, G6, G8 + phần zone-policy của G2 |
| **repo này** (microservice-full-series) | G7 + phần handler UTC của G2 |

---

## 3. Điểm mạnh (đã đạt best practice)

1. **Ranh giới package rõ ràng** — `api` / `exception` / `util` / `web`, mỗi type một trách nhiệm (SRP).
2. **DTO immutable bằng `record` + `@JsonInclude(NON_NULL)`** — toàn bộ envelope. `PageResponse`
   còn defensive-copy list (`items = List.copyOf(items)`) trong compact constructor → không rò rỉ
   tham chiếu mutable ra ngoài.
3. **Envelope thống nhất `ApiResult<T>`** với factory rõ nghĩa `ok` / `noData` / `fail`. Đặc biệt
   `noData(String)` được đặt tên khác `ok(...)` để **tránh ambiguity** với `ok(T)` khi `T=String`
   — đúng khuyến nghị **TS-004 PROPOSAL-003**, đã implement chuẩn.
4. **Cây exception phân tầng tốt** — `ApiException` abstract giữ `responseCode`/`httpStatus`/
   `userMessage`; subtype chuyên biệt. `IntegrationException` mang `externalReference`/`correlationId`;
   `UnknownResultException` mang thêm `transactionReference` và trả **HTTP 202** cho trạng thái giao
   dịch chưa xác định → **rất hợp ngữ cảnh banking/fintech** (gọi hệ thống ngoài, đối soát giao dịch).
5. **`GlobalExceptionHandler` phủ đủ ma trận lỗi input** — validation, constraint, type mismatch,
   body không đọc được, method not supported, và catch-all `Exception`.
6. **Chống log-injection** bằng `sanitize()` (strip `\n`/`\r` trước khi log URI/message) — best
   practice bảo mật thật (khả năng do FindSecBugs thúc đẩy).
7. **Scope `provided`** cho servlet-api / validation-api / spring-data-commons → không ép version
   transitive lên consumer. Hygiene tốt của library.
8. **Metadata artifact publish chuẩn** — MIT license, SCM, developer, đính kèm sources + javadoc
   jar; DevSecOps đầy đủ (SpotBugs + FindSecBugs, OWASP dependency-check, JaCoCo, SonarCloud).
9. **Deprecation đúng chuẩn** — `TraceIdFilter` đánh `@Deprecated(since="0.2.10", forRemoval=true)`
   + ghi chú migration (Sleuth/Micrometer).
10. **`PageableFactory` có bound an toàn** — `MAX_PAGE_SIZE=100`, `Math.clamp(size,1,100)`,
    `Math.max(1,page)-1` (chuyển page 1-based → 0-based) → tránh page-size vô hạn.

---

## 4. Các Gap xếp theo mức độ

### 🔴 [CAO] G1 — Thiếu Spring Boot auto-configuration (điểm trừ chính)

**Bằng chứng:** jar 0.3.0 không có
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
`GlobalExceptionHandler` là `@RestControllerAdvice`, `TraceIdFilter` là `@Component`.

**Vấn đề:** hai bean này chỉ load được vì **mọi service đều đặt main class ở `package vn.conghung`**,
còn lib nằm ở `vn.conghung.common.*` → `@SpringBootApplication` component-scan **tình cờ** với tới.
Nghĩa là lib **không tự-đóng-gói / không portable**: một consumer đặt ở package khác (vd
`com.acme.x`) sẽ **âm thầm mất** `GlobalExceptionHandler` → quay lại **HTTP 500** — đúng sự cố
**TS-001** đã ghi.

**Nghịch lý với lịch sử:** TS-001 kết luận cách fix đúng là auto-config (`.imports` +
`@AutoConfiguration`), và liệt kê `scanBasePackages` / `@Import` là cách **không nên**. Nhưng repo
thực tế lại chọn cách thứ ba — "chuẩn hoá package root `vn.conghung` cho mọi service" — vốn không
có trong TS-001 và chính là một coupling ẩn.

**Fix (repo commons):**
```java
// vn/conghung/common/autoconfigure/CommonsAutoConfiguration.java
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class CommonsAutoConfiguration { }
```
```
# META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
vn.conghung.common.autoconfigure.CommonsAutoConfiguration
```
Sau đó service chỉ cần có dependency là handler tự load — **không phụ thuộc package root**.

**Trade-off:** cách hiện tại "chạy được" và đơn giản, nhưng đánh đổi bằng tính portable + rủi ro
regression im lặng cho service tương lai. Auto-config là chuẩn ngành cho shared starter (giống mọi
`*-spring-boot-starter`).

---

### 🟠 [TB] G2 — Lệch timezone & bypass factory khi dựng envelope

**Bằng chứng:**
- `ApiResult` đóng dấu `requestDateTime` theo `Asia/Ho_Chi_Minh` (`private static final ZoneId ICT_ZONE`, dùng `OffsetDateTime.now(ICT_ZONE)` trong mọi factory).
- `order-service/.../OrderExceptionHandler.java:25` dựng `ApiResult` bằng **constructor thô** với `OffsetDateTime.now(ZoneOffset.UTC)`.

**Vấn đề:** cùng field `requestDateTime`, trên cùng một API surface, lại có **2 zone** (success =
ICT, error của order = UTC). Ngoài ra service **bypass factory**, dựng envelope tay → dễ lệch chuẩn.

**Fix:**
- **Zone-policy (repo commons):** thống nhất **UTC**; cân nhắc inject `Clock` để testable thay vì `now()` cứng.
- **Handler (repo này):** đi qua factory thay vì constructor thô; đã ghi ở **TS-017 (M3/M4)** là gộp vào debt timezone toàn cục — ở đây chỉ *nhắc lại*, không apply.

**Trade-off:** đổi ICT→UTC là breaking nhẹ cho client đang parse local time; nên làm đồng loạt
commons + account + product + order trong một nhịp, có changelog.

---

### 🟠 [TB] G3 — Library phụ thuộc stereotype-scan (`@Component` trên `TraceIdFilter`)

**Bằng chứng:** `web/TraceIdFilter.java` — `@Component` (dù đã `@Deprecated(forRemoval=true)`, body
no-op chỉ `filterChain.doFilter`).

**Vấn đề:** filter no-op vẫn bị auto-đăng-ký vào servlet filter chain (thêm 1 mắt xích vô ích).
Library **không nên** dùng stereotype phụ thuộc component-scan để đăng ký hạ tầng — cùng gốc rễ với G1.

**Fix (repo commons):** chuyển sang auto-config/conditional bean, hoặc **xoá hẳn** ở major kế tiếp
(đã đánh `forRemoval` sẵn nên đường đóng đã rõ).

---

### 🟠 [TB] G4 — Thứ tự `@RestControllerAdvice` không tường minh

**Bằng chứng:** `GlobalExceptionHandler` có catch-all `@ExceptionHandler(Exception.class)` và
**không** `@Order`. Các advice service (Order/Product/Account) cũng không `@Order`.
`OrderBusinessException extends ApiException` (đã verify).

**Vấn đề:** hiện exact-match thắng nên vẫn đúng, nhưng một **catch-all advice không xếp thứ tự** là
rủi ro tiềm ẩn — khi có nhiều advice cùng ứng viên mà thứ tự bất định, catch-all có thể che handler
cụ thể hơn ở advice khác.

**Fix (repo commons):** đánh `@Order(Ordered.LOWEST_PRECEDENCE)` cho advice global → advice của
service **luôn** ưu tiên một cách xác định. Chi phí gần như bằng 0, loại bỏ lớp rủi ro.

---

### 🟡 [Thấp–TB] G5 — Parse chuỗi message exception dễ vỡ

**Bằng chứng:** `GlobalExceptionHandler.resolveDetailMessage()` & `parseMismatchedInput()` bóc
`HttpMessageNotReadableException.getMessage()` bằng substring: `"through reference chain:"`,
`"from String \""`, `type '...'`, `"Cannot deserialize value of type"`, và cả cờ
`"StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION"`.

**Vấn đề:** phụ thuộc **format text** của Jackson/Spring theo version & locale → dễ gãy khi nâng cấp
(đặc biệt Spring Boot 4.x / Jackson 3.x đổi message).

**Fix (repo commons):** dùng **cause có cấu trúc** — `InvalidFormatException` / `MismatchedInputException`,
`getPath()` (lấy field name), `getTargetType()` (lấy type) — thay vì bóc text. Ổn định qua version.

---

### 🟡 [Thấp] G6 — Test coverage chưa rõ / không ship

**Bằng chứng:** sources jar chỉ có `main`; JaCoCo có cấu hình trong pom nhưng không thấy test source.

**Vấn đề:** `GlobalExceptionHandler` là code có **blast-radius lớn nhất toàn hệ** (mọi service phụ
thuộc contract của nó). Thiếu test cho ma trận mapping exception→status là rủi ro cho toàn fleet.

**Fix (repo commons):** thêm contract test (`@WebMvcTest`/standalone `MockMvc`) khẳng định từng loại
exception → đúng HTTP status + đúng `responseCode` + đúng shape `ErrorDetail`.

---

### 🟡 [Thấp] G7 — Version chưa quản lý tập trung

**Bằng chứng:** mỗi service lặp `<conghung-commons.version>0.3.0` trong pom riêng
(order/product/account/mail).

**Vấn đề:** dễ trôi version giữa các service (một service quên bump → chạy contract khác nhau).

**Fix (repo này):** khai 1 lần ở **parent pom `<dependencyManagement>`**, service chỉ khai
`<dependency>` không kèm version. **Ngoài scope "doc-only"** → chỉ khuyến nghị, không apply ở audit này.

---

### 🟡 [Thấp] G8 — `ErrorDetail(Object details)` không có kiểu + nullness không nhất quán

**Bằng chứng:** `ErrorDetail` bọc `Object` thô; annotation nullness (`jspecify @NonNull`) chỉ xuất
hiện ở `TraceIdFilter`, các file khác không có.

**Vấn đề:** `Object details` mất type-safety và khó document cho client (thực tế thường là
`List<ValidationError>`). Nullness không đồng bộ toàn module.

**Fix (repo commons):** document rõ các shape của `details`, hoặc dùng type hẹp hơn; chuẩn hoá
nullness bằng `package-info.java` `@NullMarked` (jspecify) cho toàn module.

---

## 5. Remediation roadmap

### (A) Thay đổi ở repo commons `c0nghung/conghung-commons` — cần checkout repo đó
| Ưu tiên | Gap | Việc | Breaking? |
|---|---|---|---|
| 1 | G1 | Thêm `@AutoConfiguration` + `AutoConfiguration.imports` cho handler | Không (chỉ thêm) |
| 2 | G4 | `@Order(LOWEST_PRECEDENCE)` cho `GlobalExceptionHandler` | Không |
| 3 | G3 | Filter no-op → auto-config hoặc xoá ở major | Có (major) |
| 4 | G5 | Parse lỗi bằng structured cause | Không (nội bộ) |
| 5 | G6 | Contract test cho handler | Không |
| 6 | G2a | Zone-policy → UTC + `Clock` | Có (phối hợp với service) |
| 7 | G8 | Document/typing `ErrorDetail` + nullness | Không |

### (B) Thay đổi ở repo này (microservice-full-series) — **không apply ở audit này**
| Gap | Việc |
|---|---|
| G7 | Đưa `conghung-commons.version` lên parent pom `<dependencyManagement>` |
| G2b | Các `*ExceptionHandler` đi qua factory + UTC nhất quán (đồng bộ khi làm G2a) |

---

## 6. Checklist khi thực thi (sau khi duyệt)

- [ ] **G1:** service ở package bất kỳ vẫn nhận `GlobalExceptionHandler` chỉ nhờ dependency (test bằng 1 module package `com.test`).
- [ ] **G4:** exception service vẫn được advice riêng bắt trước catch-all sau khi thêm `@Order`.
- [ ] **G5:** malformed JSON / sai kiểu vẫn ra message hợp lý sau khi bỏ string-scraping.
- [ ] **G2:** `requestDateTime` đồng nhất UTC ở cả success lẫn error trên mọi service.
- [ ] **G7:** mọi service dùng đúng 1 version từ parent pom.

---

## 7. Tham khảo

- `troubleshooting/TS-001-global-exception-handler-not-loaded.md` — gốc rễ vấn đề load handler (G1).
- `troubleshooting/TS-004-commons-improvement-proposals.md` — lịch sử `ApiResult.ok()`/`noData()` (đã implement).
- `troubleshooting/TS-008-propose-pagination-sort-commons.md` — nguồn gốc `PageResponse`/`PageableFactory`/`SortParser`.
- `troubleshooting/TS-017-order-service-production-hardening.md` — M3/M4 timezone debt (liên quan G2).
- Spring Boot Docs — [Creating Your Own Auto-configuration](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html).
