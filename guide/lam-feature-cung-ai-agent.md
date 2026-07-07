# Cẩm nang: Làm Feature/Task cùng AI Agent theo Best Practice (High-Performance)

> **Đối tượng:** developer làm việc với AI Agent (Claude Code) trong repo `conghung-commons`.
> **Mục tiêu:** một quy trình lặp lại được để làm task/feature **cùng** AI Agent đạt chất lượng cao + tốc độ cao, với con người là **người quyết định và chịu trách nhiệm**.
> **Ví dụ xuyên suốt:** audit [TS-018](../troubleshooting/TS-018-commons-shared-library-best-practice-audit.md) — remediation best-practice cho shared library.
> **Triết lý cốt lõi:** *Spec-Driven · Agentic · Verify-after-every-step · **Collaborate, don't command***.

---

## Mục lục

1. [Mục tiêu & triết lý](#1-mục-tiêu--triết-lý)
2. [8 nguyên tắc cộng tác High-Performance](#2-8-nguyên-tắc-cộng-tác-high-performance)
3. [Tranh luận & cộng tác với AI như một cộng sự (mục trọng tâm)](#3-tranh-luận--cộng-tác-với-ai-như-một-cộng-sự-mục-trọng-tâm)
4. [Vòng đời feature với AI Agent & phân vai](#4-vòng-đời-feature-với-ai-agent--phân-vai)
5. [Chuẩn bị `.claude/` — skill/command/workflow/hook](#5-chuẩn-bị-claude--skillcommandworkflowhook)
6. [Bước 1 — Phân tích SRS](#6-bước-1--phân-tích-srs)
7. [Bước 2 — Viết `spec.md`](#7-bước-2--viết-specmd)
8. [Bước 3 — Chia Bounded Context & Phase](#8-bước-3--chia-bounded-context--phase)
9. [Bước 4 — Chia Step & định nghĩa Done](#9-bước-4--chia-step--định-nghĩa-done)
10. [Bước 5 — TDD & Verify sau mỗi step](#10-bước-5--tdd--verify-sau-mỗi-step)
11. [Bước 6 — Dùng skill/command/workflow/hook](#11-bước-6--dùng-skillcommandworkflowhook)
12. [Bước 7 — Commit & PR](#12-bước-7--commit--pr)
13. [Template `spec.md` & `plan.md`](#13-template-specmd--planmd)
14. [Decision Record (ADR nhẹ) + bảng Risk/Impact/Trade-off](#14-decision-record-adr-nhẹ--bảng-riskimpacttrade-off)
15. [Thư viện prompt](#15-thư-viện-prompt)
16. [High-performance tips & anti-patterns](#16-high-performance-tips--anti-patterns)
17. [Phụ lục — liên kết & checklist sẵn sàng](#17-phụ-lục--liên-kết--checklist-sẵn-sàng)

---

## 1. Mục tiêu & triết lý

Làm việc với AI Agent hiệu quả **không phải** là gõ "viết code X" rồi copy kết quả. Nó là một quy trình kỹ thuật có kỷ luật:

- **Spec-Driven** — chốt *cái gì / vì sao* (`spec.md`) trước *làm thế nào* (`plan.md`), trước khi gõ dòng code đầu tiên.
- **Agentic** — dùng subagent (Explore/Plan) và workflow để chia việc, giữ context sạch, chạy song song.
- **Verify-after-every-step** — mỗi bước nhỏ đều có test + kiểm chứng hành vi thật; không tin suông.
- **Collaborate, don't command** — coi AI là **cộng sự phản biện**: yêu cầu nhiều phương án, tranh luận, bắt nêu rủi ro; **dev quyết & chịu trách nhiệm**.

> Kết quả: nhanh hơn (ít làm lại, ít lạc hướng) và an toàn hơn (ít bug, ít hallucination, quyết định có vết).

## 2. 8 nguyên tắc cộng tác High-Performance

1. **Spec là single source of truth** — mọi tranh cãi về phạm vi quay về `spec.md`. Thay đổi phạm vi ⇒ sửa spec trước.
2. **Plan trước khi code** — dùng plan mode; không để agent nhảy vào sửa file khi chưa có kế hoạch được duyệt.
3. **TDD cho logic tiền** — viết/sửa test trước hoặc cùng code; đỏ → xanh → refactor.
4. **Chia step nhỏ verify được** — mỗi step độc lập chạy test/verify + 1 commit; step to là mầm bug và khó review.
5. **Dùng subagent Explore/Plan để tiết kiệm context** — fan-out tìm kiếm, chỉ giữ *kết luận + file path*, không nhồi cả file vào context chính.
6. **Luôn lint + test trước khi nói "xong"** — "xong" = đã chạy `./mvnw test` (hoặc verify) xanh, không phải "code trông có vẻ đúng".
7. **Không commit khi chưa được yêu cầu** — dev kiểm soát thời điểm commit/push.
8. **Ghi memory những quyết định phi-hiển-nhiên** — để phiên sau (người/agent) không phải suy lại; quyết định lớn → ADR.

## 3. Tranh luận & cộng tác với AI như một cộng sự (mục trọng tâm)

> Đây là điểm phân biệt giữa "dùng AI như máy sinh code" và "làm việc với AI như một senior đồng nghiệp". Chất lượng đầu ra phụ thuộc phần lớn vào **cách bạn đối thoại**.

### 3.1 Mindset pair-programming

Coi AI là cộng sự để **thảo luận**, không phải cỗ máy nhận lệnh. Dùng **prompt mở** thay vì **prompt đóng**:

| ❌ Prompt đóng (ra lệnh) | ✅ Prompt mở (cộng tác) |
|---|---|
| "Viết auto-config cho handler." | "Có mấy cách để handler tự load mà không phụ thuộc package root? Phân tích đánh đổi giúp tôi." |
| "Đổi timezone sang UTC." | "Đổi ICT→UTC có rủi ro gì? Có cách nào tránh breaking không, vì sao nên/không nên?" |
| "Sửa lỗi parse JSON." | "Cách parse hiện tại dễ vỡ ở đâu? Có cách ổn định hơn qua version không?" |

### 3.2 Vòng lặp **Đề xuất → Phản biện → Chốt**

1. **Đề xuất:** yêu cầu AI đưa **≥2 phương án** kèm bảng so sánh ưu/nhược/trade-off.
2. **Phản biện:** dev hỏi ngược — *"tại sao không chọn cách kia?"*, *"cách này hỏng khi nào?"*. Cho phép AI **bất đồng có lập luận**: nêu rõ trong prompt *"nếu bạn thấy tôi sai, hãy nói thẳng kèm lý do"*.
3. **Chốt:** dev quyết → ghi [ADR](#14-decision-record-adr-nhẹ--bảng-riskimpacttrade-off).

**Mẫu hội thoại tranh luận thật (TS-018 · G1 — wiring handler):**

> **Dev:** Handler đang không load nếu service ở package khác. Nên fix thế nào? Đừng viết code vội, cho tôi phương án.
> **AI:** 3 cách: (A) `@AutoConfiguration` + `AutoConfiguration.imports`; (B) bắt consumer `@Import`/`scanBasePackages`; (C) chuẩn hoá mọi service về package `vn.conghung`. Tôi *khuyến nghị A* — chuẩn ngành cho starter, portable, không breaking. B đẩy việc sang consumer; C là coupling ẩn, đúng thứ TS-001 nói "không nên".
> **Dev:** C đang "chạy được" mà, sao phải đổi?
> **AI:** "Chạy được" nhờ trùng package — một service tương lai đặt ở `com.acme` sẽ **mất handler âm thầm** → HTTP 500. Đó là regression im lặng, không có test nào bắt. A loại bỏ hẳn lớp rủi ro này với chi phí gần như 0. Nếu anh vẫn muốn giữ C vì ngại thêm dependency, tôi không đồng ý: `spring-boot-autoconfigure` scope `provided` không ép version lên ai.
> **Dev:** OK, chọn A. Ghi ADR.

→ Xem kết quả: [ADR-001](../docs/decisions/ADR-001-autoconfig-vs-componentscan.md).

*(Ví dụ generic khác:* "đặt logic cap 50% ở step `enforceGlobalCap` tập trung, hay rải trong từng route?" — cùng khuôn: AI nêu 2 phương án + trade-off + impact tới `revalidateVoucherOnCartChange`, dev quyết, ghi ADR.)*

### 3.3 Bắt buộc AI nêu **Risk / Impact / Trade-off TRƯỚC khi code**

Trước mọi thay đổi không tầm thường, dùng command [`/risk-check`](../.claude/commands/risk-check.md) hoặc prompt:

> "Trước khi làm, liệt kê: (1) rủi ro & giả định, (2) ảnh hưởng tới phần nào khác trong hệ thống (cross-module, cart totals, cache, migration, backward-compat), (3) các trade-off và phương án bạn *khuyến nghị* + lý do."

Kết quả đổ vào **Decision Record** (mục 14).

### 3.4 Developer là người quyết định & chịu trách nhiệm

| AI Agent làm | Dev quyết |
|---|---|
| Explore, đề xuất phương án, viết code/test theo hướng đã chốt, verify, nêu rủi ro | Chọn phương án, duyệt spec/plan, review diff, chọn thời điểm commit/merge, chịu trách nhiệm cuối |

**Checklist trước khi merge:** *"Tôi đã hiểu thay đổi này chưa? Tôi giải thích được nó cho người khác không?"* — **Không merge code mình không hiểu.**

### 3.5 Verify để tin, đừng tin để verify (chống hallucination)

Mọi khẳng định của AI — "API này tồn tại", "Jackson bản 3", "file kia có hàm đó", con số benchmark — phải **kiểm chứng** bằng đọc code / chạy lệnh read-only trước khi tin. Ví dụ TS-018: đừng tin "dùng `com.fasterxml.jackson.databind.exc.InvalidFormatException`" — kiểm tra và phát hiện Spring Boot 4 dùng **Jackson 3** (`tools.jackson.databind.exc.*`). Một lần verify tiết kiệm một giờ debug.

## 4. Vòng đời feature với AI Agent & phân vai

```
        ┌─────────────────────────── vòng Đề xuất→Phản biện→Chốt (ghi ADR) ───────────────────────────┐
        │                                                                                              │
  SRS/Audit ──► spec.md ──► plan.md ──► tasks.md ──► [Execute từng step: code + TDD + verify] ──► Review ──► Commit ──► PR ──► CI gate
  (WHAT+WHY)   (duyệt)     (HOW)      (step+test)                                              (workflow)  (conv.)         (Sonar)
```

- **Con người:** đặt mục tiêu, duyệt spec/plan, phản biện, quyết định, review diff, bấm commit/merge.
- **AI Agent:** đọc SRS, explore (subagent), đề xuất phương án + rủi ro, sinh spec/plan/tasks, viết code+test, verify, tự report trung thực.

## 5. Chuẩn bị `.claude/` — skill/command/workflow/hook

| Artifact | File | Vai trò | Khi nào dùng |
|---|---|---|---|
| **skill** `feature-kickoff` | [.claude/skills/feature-kickoff/SKILL.md](../.claude/skills/feature-kickoff/SKILL.md) | Biến SRS → spec/plan/tasks rồi vào plan mode | Đầu mỗi feature |
| **skill** `java-verify` | [.claude/skills/java-verify/SKILL.md](../.claude/skills/java-verify/SKILL.md) | Chuỗi lệnh test/verify Maven + quy tắc TDD | Sau mỗi step, trước commit |
| **command** `/risk-check` | [.claude/commands/risk-check.md](../.claude/commands/risk-check.md) | Ép nêu Risk/Impact/Trade-off + ≥2 phương án trước khi code | Trước quyết định kỹ thuật lớn |
| **workflow** `review-changes` | [.claude/workflows/review-changes.js](../.claude/workflows/review-changes.js) | Fan-out review đa agent + verify đối kháng | Phase review (cần opt-in) |
| **hook** `Stop` | [.claude/settings.json](../.claude/settings.json) | Chặn kết thúc nếu `./mvnw test` đỏ (khi có đổi `.java`/`pom.xml`) | Chạy nền tự động |
| **spec/plan/tasks/ADR** | [docs/](../docs/) | Tài liệu WHAT/HOW/step/quyết định | Suốt vòng đời |

> Skill/command là project-scoped (commit vào repo) nên **cả team + agent** dùng chung một quy trình.

## 6. Bước 1 — Phân tích SRS

SRS ở đây là audit [TS-018](../troubleshooting/TS-018-commons-shared-library-best-practice-audit.md). Cách bóc:

1. **Đọc hết, không lướt.** Dùng subagent `Explore` song song cho codebase liên quan; giữ *kết luận + file path*.
2. **Liệt kê yêu cầu truy vết được** — mỗi gap → 1 requirement có ID (G1→R1, ...).
3. **Phân loại severity** 🔴 Cao / 🟠 TB / 🟡 Thấp để xếp thứ tự.
4. **Khoanh scope theo repo** — TS-018 tách rõ: repo commons làm **G1/G3/G4/G5/G6/G2a/G8**; G7/G2b thuộc repo microservice → **ngoài scope**. Ghi rõ để tránh làm lan.
5. **Đánh dấu giả định cần verify** — vd "Spring Boot 4 dùng Jackson mấy?" → verify ngay, đừng để tới lúc code.

→ Kết quả: [docs/specs/PROJ-9-ts018.spec.md](../docs/specs/PROJ-9-ts018.spec.md).

## 7. Bước 2 — Viết `spec.md`

`spec.md` chỉ nói **WHAT + WHY**, tuyệt đối không bàn giải pháp kỹ thuật. Bắt buộc có **Acceptance Criteria verify được** — đây là hợp đồng "done". Ví dụ (trích TS-018):

- *AC1:* Service ở package bất kỳ (vd `com.test`) vẫn nhận `GlobalExceptionHandler` **chỉ nhờ có dependency**.
- *AC4:* `requestDateTime` có offset **UTC** ở cả success lẫn error.

Dùng [template spec](../docs/specs/_TEMPLATE.spec.md). Xin dev duyệt trước khi sang plan.

## 8. Bước 3 — Chia Bounded Context & Phase

**Phase = Bounded Context** (mượn từ DDD): gom theo *vùng trách nhiệm/contract*, **không** theo file lẻ. Mỗi bounded context là một vùng có ranh giới rõ, ít rò rỉ ra ngoài — review & rollout theo vùng dễ suy luận, giảm xung đột.

TS-018 chia 3 bounded context:

| Phase (Bounded Context) | Concern (chịu trách nhiệm gì) | Gap | Breaking? |
|---|---|---|---|
| **P1 — Bootstrap & Portability** | Cách lib **tự wire** vào Spring container | G1 auto-config, G3 gỡ `@Component` | G1 không · G3 nhẹ |
| **P2 — Error-Handling Contract** | Bề mặt **lỗi API** của `GlobalExceptionHandler` | G4 `@Order`, G5 structured parse, G6 contract test | Không |
| **P3 — Response Envelope Contract** | Ngữ nghĩa **DTO trả về** (`ApiResult`/`ErrorDetail`) | G2a UTC, G8 nullness/typing | G2a **có** |

> Lý do gom: mỗi phase chạm cùng một nhóm file lõi và cùng một "hợp đồng" với consumer. → chi tiết [docs/plans/PROJ-9-ts018.plan.md](../docs/plans/PROJ-9-ts018.plan.md).

## 9. Bước 4 — Chia Step & định nghĩa Done

Trong mỗi phase, mỗi **gap = 1 step**. Nguyên tắc step:

- **Nhỏ nhất mà vẫn verify được độc lập** (đừng gộp 3 gap vào 1 commit).
- **1 step = 1 Conventional Commit** rõ nghĩa.
- **Định nghĩa Done của step:** (1) code xong, (2) test tương ứng xanh, (3) `./mvnw test` xanh, (4) commit, (5) cập nhật trạng thái trong [tasks.md](../docs/tasks/PROJ-9-ts018.tasks.md).

Ví dụ bảng step (rút gọn):

| Step | Gap | Nội dung | Test/Verify | Commit |
|---|---|---|---|---|
| S1.3 | G1 | `CommonsAutoConfiguration` + `.imports` | ContextRunner load handler (AC1) | `feat(autoconfigure): ...` |
| S4.2 | G5 | Structured Jackson 3 cause | post JSON sai kiểu → message đúng (AC3) | `fix(exception): ...` |
| S7.1 | G2a | ICT→UTC | assert offset UTC (AC4) | `feat(api)!: ...` |

## 10. Bước 5 — TDD & Verify sau mỗi step

**Loại test theo gap:**

- **Unit** — logic thuần (vd `SortParser`, `PageableFactory`).
- **Context test** — `ApplicationContextRunner` chứng minh auto-config load *độc lập package* (G1/AC1).
- **Contract test** — standalone `MockMvc` (`MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler())`) khẳng định *exception → HTTP status + responseCode + shape* (G4/G5/G6).
- **Assertion hành vi** — vd `ApiResult...requestDateTime().getOffset()` = `ZoneOffset.UTC` (G2a/AC4).

**Lệnh (chi tiết ở skill [java-verify](../.claude/skills/java-verify/SKILL.md)):**

| Khi nào | Lệnh |
|---|---|
| Vòng lặp TDD 1 class | `./mvnw test -Dtest=<TênTest>` |
| Sau mỗi step | `./mvnw test` |
| Ranh giới phase / trước commit lớn | `./mvnw clean verify -Ddependency-check.skip=true` |
| Trước push (có NVD key) | `./mvnw clean verify` |

**Definition of verify:** không chỉ "test xanh" mà **quan sát hành vi thật** — dựng input thật (post JSON, dựng exception thật), xác nhận output đúng. Đặc biệt với G5: verify `getCause()` đúng là `tools.jackson.databind.exc.*` (Jackson 3) trước khi tin import.

## 11. Bước 6 — Dùng skill/command/workflow/hook

- **`/feature-kickoff`** — khi bắt đầu: đọc SRS → sinh spec/plan/tasks → vào plan mode.
- **`/risk-check <thay đổi>`** — trước quyết định lớn: ép nêu Risk/Impact/Trade-off + ≥2 phương án → dev quyết → ghi ADR.
- **`/java-verify`** — sau mỗi step / trước commit: chạy đúng chuỗi test/verify.
- **workflow `review-changes`** — cuối phase, muốn review kỹ: nói *"use a workflow: review-changes"* để fan-out review đa dimension (correctness/security/simplicity) + verify đối kháng từng finding (workflow cần **opt-in rõ ràng**).
- **hook `Stop`** — tự chạy `./mvnw -q test` khi có đổi `.java`/`pom.xml` và **chặn kết thúc** nếu test đỏ. (Chi phí: chạy test mỗi lần Stop khi có đổi Java — muốn nhẹ hơn thì đổi matcher/tạm gỡ trong [settings.json](../.claude/settings.json).)

## 12. Bước 7 — Commit & PR

- **Conventional Commits** (release-please parse để bump semver + sinh CHANGELOG):
  - `feat:` tính năng · `fix:` sửa lỗi · `test:` thêm test · `refactor:` dọn nội bộ · `docs:` tài liệu · `build:` dependency/pom.
  - **Breaking**: thêm `!` (vd `feat(api)!:`) + footer `BREAKING CHANGE: <mô tả>` (TS-018 G2a).
- **1 step → 1 commit** rõ nghĩa; nội dung khớp scope.
- **PR**: tiêu đề tham chiếu ticket/audit; body liệt kê gap đã xử lý + note breaking. CI chạy `mvn clean verify` (test + OWASP + SpotBugs) + **SonarCloud quality gate** (`qualitygate.wait=true`) phải PASS trước merge (AC8).
- **Branch**: theo convention repo — `feature/PROJ-9-ts018-shared-library-best-practices`.

## 13. Template `spec.md` & `plan.md`

Dùng trực tiếp: [_TEMPLATE.spec.md](../docs/specs/_TEMPLATE.spec.md) · [_TEMPLATE.plan.md](../docs/plans/_TEMPLATE.plan.md). Bản đã điền cho TS-018: [spec](../docs/specs/PROJ-9-ts018.spec.md) · [plan](../docs/plans/PROJ-9-ts018.plan.md).

**Trích spec (What/Why + AC):**

```markdown
## 1. Vấn đề & Mục tiêu (Why)
- Nỗi đau: lib thiếu auto-config → handler chỉ load nhờ trùng package root → service package khác mất handler → HTTP 500.
- Mục tiêu: lib portable đúng chuẩn starter, có contract test, nhất quán timezone/nullness.
## 4. Acceptance Criteria
- [ ] AC1: service ở package bất kỳ vẫn nhận handler chỉ nhờ dependency.
- [ ] AC4: requestDateTime offset UTC ở cả success lẫn error.
```

**Trích plan (HOW — Phase 1 / Bootstrap & Portability):**

```markdown
### P1 — Bootstrap & Portability
- G1: + spring-boot-autoconfigure (provided) → CommonsAutoConfiguration
      (@AutoConfiguration @Import(GlobalExceptionHandler.class))
      → META-INF/spring/....AutoConfiguration.imports. Quyết định: ADR-001.
- G3: TraceIdFilter — gỡ @Component (giữ class + deprecation).
```

## 14. Decision Record (ADR nhẹ) + bảng Risk/Impact/Trade-off

Mỗi **quyết định quan trọng** ghi 1 ADR ngắn: *Bối cảnh → Phương án (kèm trade-off) → Rủi ro & Impact → Quyết định của dev → Lý do → Hệ quả*. Lưu ở [docs/decisions/](../docs/decisions/) (hoặc nhúng trong plan.md nếu nhỏ).

Template: [_TEMPLATE.adr.md](../docs/decisions/_TEMPLATE.adr.md). Ví dụ đã điền: [ADR-001 auto-config vs scan](../docs/decisions/ADR-001-autoconfig-vs-componentscan.md) · [ADR-002 ICT→UTC](../docs/decisions/ADR-002-ict-to-utc.md).

**Bảng Risk/Impact/Trade-off (khung tối thiểu):**

| Mục | Nội dung |
|---|---|
| **Phương án** (≥2) | A / B (/ C) + ưu · nhược · trade-off |
| **Rủi ro** | Điều có thể hỏng + mức độ/xác suất |
| **Impact cross-cutting** | module khác, contract, cache, migration, backward-compat, breaking? |
| **Giả định cần verify** | điều chưa chắc — đánh dấu để kiểm chứng |
| **Khuyến nghị AI** | chọn gì + vì sao (được bất đồng có lập luận) |
| **Quyết định Dev** | chọn gì + lý do + ☑ đã hiểu & chịu trách nhiệm |

*Ví dụ điền (generic):* "Đặt logic **cap 50%** ở step `enforceGlobalCap` (tập trung) thay vì trong từng route — trade-off: một chỗ dễ maintain nhưng phải cẩn thận thứ tự áp dụng; **impact** tới `revalidateVoucherOnCartChange` khi giỏ hàng đổi; **ai chịu trách nhiệm:** dev X."

## 15. Thư viện prompt

**Nhóm cộng tác (quan trọng nhất):**
- *"Trước khi làm, nêu risk/impact/trade-off + ≥2 phương án và khuyến nghị của bạn, đừng code vội."*
- *"Đề xuất ≥2 cách khác nhau, so sánh, rồi nói bạn chọn cách nào và vì sao."*
- *"Phản biện lựa chọn của tôi: nếu tôi sai hãy nói thẳng kèm lý do."*
- *"Chỗ này ảnh hưởng gì tới phần khác trong hệ thống không? (cross-module, cache, contract, migration)"*
- *"Bạn đang giả định gì ở đây? Cái nào chưa verify?"*

**Nhóm explore/plan:**
- *"Explore <vùng> bằng subagent song song, chỉ trả về kết luận + file path."*
- *"Vào plan mode, chia theo bounded context, nêu tái sử dụng gì có sẵn."*

**Nhóm verify/commit:**
- *"Verify hành vi thật của thay đổi, đừng chỉ chạy test — dựng input thật rồi quan sát output."*
- *"Chưa commit. Cho tôi xem diff + tóm tắt trước."*

## 16. High-performance tips & anti-patterns

**Nên:**
- Vào **plan mode** trước việc không tầm thường; duyệt plan rồi mới code.
- **Explore song song** (nhiều subagent 1 lượt) khi phạm vi rộng.
- **Commit nhỏ**, mỗi step một commit; dễ review, dễ revert.
- **Verify trước khi nói "xong"**; report trung thực (test đỏ thì nói đỏ).
- **Cập nhật `tasks.md` liên tục** — nó là bảng điều khiển tiến trình cho cả người & agent.

**Tránh (anti-patterns):**
- ❌ Ra lệnh "viết X" mà bỏ qua phương án/rủi ro → mất cơ hội bắt lỗi sớm.
- ❌ Để AI **tự quyết breaking change** — luôn qua `/risk-check` + ADR + dev duyệt.
- ❌ Tin khẳng định của AI mà không verify (hallucination về API/version/file).
- ❌ Step khổng lồ, commit "misc fixes" gộp nhiều việc.
- ❌ Merge code mình không hiểu.
- ❌ Nhồi cả file lớn vào context chính thay vì để subagent tóm tắt.

## 17. Phụ lục — liên kết & checklist sẵn sàng

**Artifact của quy trình này:**
- Guide: `guide/lam-feature-cung-ai-agent.md` (file này)
- Templates: [spec](../docs/specs/_TEMPLATE.spec.md) · [plan](../docs/plans/_TEMPLATE.plan.md) · [adr](../docs/decisions/_TEMPLATE.adr.md)
- Ví dụ TS-018: [spec](../docs/specs/PROJ-9-ts018.spec.md) · [plan](../docs/plans/PROJ-9-ts018.plan.md) · [tasks](../docs/tasks/PROJ-9-ts018.tasks.md) · [ADR-001](../docs/decisions/ADR-001-autoconfig-vs-componentscan.md) · [ADR-002](../docs/decisions/ADR-002-ict-to-utc.md)
- `.claude/`: [feature-kickoff](../.claude/skills/feature-kickoff/SKILL.md) · [java-verify](../.claude/skills/java-verify/SKILL.md) · [/risk-check](../.claude/commands/risk-check.md) · [review-changes workflow](../.claude/workflows/review-changes.js) · [settings.json (hook)](../.claude/settings.json)
- SRS gốc: [TS-018](../troubleshooting/TS-018-commons-shared-library-best-practice-audit.md)

**Checklist "sẵn sàng bắt đầu một feature":**
- [ ] Đã đọc & hiểu SRS; đã khoanh scope (trong/ngoài).
- [ ] `spec.md` có Acceptance Criteria **verify được**, đã được dev duyệt.
- [ ] `plan.md` chia theo bounded context; quyết định lớn đã có ADR.
- [ ] `tasks.md` liệt kê step + test + verify + kiểu commit.
- [ ] Branch đã tạo theo convention.
- [ ] Đã chạy `/risk-check` cho mọi thay đổi rủi ro/breaking.
- [ ] Hook/skill/command đã sẵn trong `.claude/`.

> Xong checklist ⇒ bắt đầu thực thi **từng step**: code → test → verify → commit → cập nhật `tasks.md`. Lặp tới hết, rồi mở PR.
