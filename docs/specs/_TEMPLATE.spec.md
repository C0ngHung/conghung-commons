# Spec: <TÊN FEATURE / TICKET>

> **ID:** `<PROJ-N>` · **Nguồn (SRS):** `<link tới audit/ticket/yêu cầu>` · **Ngày:** `<YYYY-MM-DD>`
> **Trạng thái:** 📝 DRAFT → ✅ APPROVED · **Người duyệt:** `<tên dev>`
> **Loại:** spec = **WHAT + WHY**. KHÔNG bàn HOW (giải pháp kỹ thuật ở `plan.md`).

---

## 1. Vấn đề & Mục tiêu (Why)

- **Bối cảnh / nỗi đau:** `<mô tả ngắn vì sao cần làm — ai đau, đau ở đâu>`
- **Mục tiêu:** `<kết quả mong muốn sau khi xong, đo được>`
- **Không làm gì (Non-goals):** `<khoanh vùng để tránh scope creep>`

## 2. Phạm vi (Scope)

| Trong scope | Ngoài scope |
|---|---|
| `<hạng mục>` | `<hạng mục — kèm lý do & nơi xử lý khác>` |

## 3. Yêu cầu (What) — có thể truy vết

| # | Yêu cầu | Mức ưu tiên | Nguồn |
|---|---|---|---|
| R1 | `<mô tả yêu cầu>` | 🔴 Cao / 🟠 TB / 🟡 Thấp | `<G1 / mục audit / ticket>` |

## 4. Acceptance Criteria (nghiệm thu)

> Mỗi tiêu chí phải **verify được** (chạy test / quan sát hành vi). Đây là hợp đồng "done".

- [ ] AC1 — `<điều kiện quan sát được, vd: service ở package bất kỳ vẫn nhận handler chỉ nhờ dependency>`
- [ ] AC2 — `<...>`

## 5. Ràng buộc & Giả định

- **Ràng buộc:** `<version, backward-compat, breaking?, môi trường>`
- **Giả định (cần verify):** `<điều đang cho là đúng nhưng phải kiểm chứng — đánh dấu để không hallucinate>`

## 6. Rủi ro nổi bật (chi tiết ở ADR)

- `<rủi ro cấp cao — link tới docs/decisions/ADR-*.md nếu đã quyết>`

## 7. Liên kết

- Plan: `docs/plans/<PROJ-N>.plan.md`
- Tasks: `docs/tasks/<PROJ-N>.tasks.md`
- Decisions: `docs/decisions/ADR-*.md`
