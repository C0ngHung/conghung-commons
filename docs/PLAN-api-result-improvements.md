# PLAN: conghung-commons `ApiResult` Improvements

> **Branch:** `feature/MICRO-002-commons-api-result-improvements`
> **Release target:** `0.2.11` → `0.2.12`
> **Source ticket:** MICRO-002 (`account-service` development)
> **Project type:** BACKEND (Java library — no frontend, no mobile)
> **Plan created:** 2026-06-10

---

## Overview

Trong quá trình phát triển `account-service`, phát sinh 3 nhóm cải tiến cho `conghung-commons`:

1. Các **void operations** (delete, logout...) phải dùng `ApiResult.ok(null)` → trông awkward
2. Thiếu factory method `noData(String)` cho trường hợp có message nhưng không có data
3. Chưa có documentation về design decision `204 vs 200 + ApiResult`

Tất cả thay đổi đều **backward compatible** — chỉ thêm mới, không sửa API cũ.

---

## Success Criteria

- [ ] `ApiResult.ok()` hoạt động đúng → trả `ApiResult<Void>` với `data: null`
- [ ] `ApiResult.noData("msg")` hoạt động đúng → trả `ApiResult<Void>` với `result.description = msg`
- [ ] Không có ambiguity lúc compile: `ApiResult.ok("string")` vẫn resolve sang `ok(T data)` với `T = String`
- [ ] Javadoc trên `ApiResult` có note về `204 vs 200` design decision
- [ ] Unit tests pass cho 2 factory methods mới
- [ ] `mvn test` green
- [ ] Version bump `0.2.12-SNAPSHOT` → `0.2.12` (release)
- [ ] README cập nhật usage examples

---

## Tech Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Language | Java 21 | Project constraint |
| Framework | Spring Boot 4.0.6 | Project constraint |
| Build | Maven | Project constraint |
| Test | JUnit 5 + Spring Boot Test | Already in pom.xml |
| Publish | GitHub Packages | `distributionManagement` đã cấu hình |

---

## File Structure (files sẽ thay đổi)

```
src/
├── main/java/vn/conghung/common/api/
│   └── ApiResult.java              ← THÊM ok() + noData(String) + Javadoc
└── test/java/vn/conghung/
    └── common/api/
        └── ApiResultTest.java      ← TẠO MỚI (unit tests)
pom.xml                             ← BUMP version 0.2.12-SNAPSHOT → 0.2.12
README.md                           ← CẬP NHẬT usage examples
CHANGELOG.md                        ← CẬP NHẬT release notes
```

---

## Task Breakdown

### PHASE 1 — Code Changes (Core)

---

#### TASK-001: Thêm `ApiResult.ok()` — no-arg factory method

**Agent:** `backend-specialist`
**Skill:** `clean-code`
**Priority:** P0 (blocker cho test)

**INPUT:**
```java
// ApiResult.java hiện tại — chỉ có:
public static <T> ApiResult<T> ok(T data) { ... }
public static <T> ApiResult<T> ok(String description, T data) { ... }
```

**OUTPUT:**
```java
/**
 * Factory method for void operations (delete, soft-delete, logout, etc.)
 * where no data needs to be returned.
 *
 * <p>Usage: {@code return ResponseEntity.ok(ApiResult.ok());}
 *
 * @return ApiResult with COMMON_SUCCESS code and null data
 */
public static ApiResult<Void> ok() {
    return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS), null, null, OffsetDateTime.now(ICT_ZONE));
}
```

**VERIFY:**
- Compile thành công
- `ApiResult.ok()` return type là `ApiResult<Void>`
- `ApiResult.ok("string")` vẫn compile và resolve sang `ok(T data)` (không bị ambiguity)

---

#### TASK-002: Thêm `ApiResult.noData(String description)`

**Agent:** `backend-specialist`
**Skill:** `clean-code`
**Priority:** P0 (blocker cho test)
**Depends on:** TASK-001

**Lý do dùng `noData` thay vì `ok(String)`:**
> `ApiResult.ok("msg")` hiện tại resolve sang `ok(T data)` với `T = String` → data = "msg"
> Nếu thêm `ok(String)` → `Void`, Java compiler bị ambiguity.
> Đặt tên khác `noData(String)` giải quyết hoàn toàn.

**INPUT:** File `ApiResult.java` sau TASK-001

**OUTPUT:**
```java
/**
 * Factory method for void operations with a custom description message.
 *
 * <p>Naming: {@code noData} instead of {@code ok(String)} to avoid compile-time
 * ambiguity with {@code ok(T data)} when {@code T = String}.
 *
 * <p>Usage: {@code return ResponseEntity.ok(ApiResult.noData("User deleted successfully"));}
 *
 * @param description custom success message
 * @return ApiResult with COMMON_SUCCESS code, custom description, and null data
 */
public static ApiResult<Void> noData(String description) {
    return new ApiResult<>(ResultInfo.of(ResponseCode.COMMON_SUCCESS, description), null, null, OffsetDateTime.now(ICT_ZONE));
}
```

**VERIFY:**
- Compile thành công
- `ApiResult.noData("msg")` return type là `ApiResult<Void>`
- `ApiResult.ok("msg")` vẫn hoạt động bình thường (không bị ambiguity)

---

#### TASK-003: Thêm class-level Javadoc — design decision note

**Agent:** `backend-specialist`
**Skill:** `documentation-templates`
**Priority:** P1
**Depends on:** TASK-001, TASK-002

**OUTPUT:** Thêm class-level Javadoc lên `ApiResult` record với note:
- `200 OK + ApiResult<Void>` vs `204 No Content` decision
- Usage examples cho `ok()` và `noData()`
- Anti-pattern examples

**VERIFY:**
- `mvn javadoc:javadoc` không có error/warning trên file này
- Design decision note xuất hiện trong generated Javadoc HTML

---

### PHASE 2 — Test Coverage

---

#### TASK-004: Viết Unit Tests

**Agent:** `backend-specialist`
**Skill:** `testing-patterns`
**Priority:** P1
**Depends on:** TASK-001, TASK-002

**File mới:** `src/test/java/vn/conghung/common/api/ApiResultTest.java`

| Test ID | Method Under Test | Assert |
|---------|------------------|--------|
| T001 | `ok()` | result.code = COMMON_SUCCESS, data = null, error = null |
| T002 | `ok()` | result.description = defaultMessage của COMMON_SUCCESS |
| T003 | `ok()` | requestDateTime != null (ICT timezone) |
| T004 | `noData(String)` | result.description = input string |
| T005 | `noData(String)` | data = null, error = null |
| T006 | `ok(T)` với T=String | data = "someString", không resolve sang noData |
| T007 | `ok(T)` existing | backward compat check (không bị break) |

**VERIFY:**
- `mvn test` GREEN
- Tất cả 7 test case pass

---

### PHASE 3 — Release Preparation

---

#### TASK-005: Bump version `0.2.12-SNAPSHOT` → `0.2.12`

**Priority:** P2
**Depends on:** TASK-004 (tests phải green trước)

**File:** `pom.xml` line 14
- Trước: `<version>0.2.12-SNAPSHOT</version>`
- Sau: `<version>0.2.12</version>`

**VERIFY:** `mvn help:evaluate -Dexpression=project.version -q -DforceStdout` → `0.2.12`

---

#### TASK-006: Cập nhật `CHANGELOG.md`

**Priority:** P2
**Depends on:** TASK-005

**OUTPUT:** Thêm entry mới vào đầu CHANGELOG:

```markdown
## [0.2.12] - 2026-06-10

### Added
- `ApiResult.ok()` — no-arg factory method for void operations (delete, logout, etc.)
- `ApiResult.noData(String description)` — void operation with custom success message
- Javadoc design decision note: `200 OK + ApiResult<Void>` vs `204 No Content`

### Notes
- All changes are backward compatible
- Originated from `account-service` MICRO-002 development
```

---

#### TASK-007: Cập nhật `README.md` — Usage examples

**Priority:** P2
**Depends on:** TASK-005

**OUTPUT:** Thêm vào section ApiResult usage:
```markdown
// Void operations (delete, logout, soft-delete):
return ResponseEntity.ok(ApiResult.ok());
return ResponseEntity.ok(ApiResult.noData("User deleted successfully"));
```

---

#### TASK-008: Build & Publish lên GitHub Packages

**Priority:** P3 (cuối cùng)
**Depends on:** TASK-005, TASK-006, TASK-007

```bash
mvn clean deploy -DskipTests
```

> ⚠️ Cần `GITHUB_TOKEN` trong `~/.m2/settings.xml`

**VERIFY:** Package visible trên GitHub Packages page

---

## Task Dependency Graph

```
TASK-001 (ok())
    └─► TASK-002 (noData)
            ├─► TASK-003 (Javadoc)
            └─► TASK-004 (Tests)
                    └─► TASK-005 (version bump)
                                ├─► TASK-006 (CHANGELOG)  ─┐
                                └─► TASK-007 (README)    ─-+─► TASK-008 (publish)
```

**Parallel được:** TASK-006 và TASK-007 sau TASK-005.

---

## Risk & Rollback

| Risk | Likelihood | Mitigation |
|------|-----------|-----------|
| Java compiler ambiguity `ok(String)` vs `noData(String)` | ✅ Giải quyết bằng tên `noData` | N/A |
| `mvn deploy` fail do thiếu GitHub token | Medium | Kiểm tra `~/.m2/settings.xml` trước |
| Downstream services bị break | Low | Chỉ thêm mới, không đổi API cũ |

**Rollback:** `git revert` hoặc pin downstream tại `0.2.11` nếu có issue.

---

## Phase X: Final Verification Checklist

- [ ] **TASK-001:** `ApiResult.ok()` compile và return đúng type
- [ ] **TASK-002:** `ApiResult.noData(String)` compile và return đúng type
- [ ] **TASK-003:** Javadoc có design decision note
- [ ] **TASK-004:** `mvn test` GREEN — tất cả 7 test cases pass
- [ ] **TASK-005:** Version = `0.2.12` (no SNAPSHOT)
- [ ] **TASK-006:** CHANGELOG có entry `[0.2.12]`
- [ ] **TASK-007:** README có usage examples mới
- [ ] **TASK-008:** Package visible trên GitHub Packages
- [ ] **Backward compat:** `ApiResult.ok(null)`, `ApiResult.ok(T)`, `ApiResult.ok(String, T)` vẫn hoạt động
- [ ] **Git commit:** `feat[MICRO-002](api-result): add ok() and noData() factory methods`
- [ ] **PR:** Tạo PR từ `feature/MICRO-002-commons-api-result-improvements` → `main`
