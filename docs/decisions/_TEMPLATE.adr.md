# ADR-<NNN>: <Tiêu đề quyết định>

> **Ngày:** `<YYYY-MM-DD>` · **Trạng thái:** 🟨 PROPOSED → ✅ ACCEPTED / ❌ REJECTED / ♻️ SUPERSEDED bởi ADR-`<NNN>`
> **Feature:** `<PROJ-N>` · **Người quyết định (dev):** `<tên — người chịu trách nhiệm>`
> **AI Agent đề xuất:** có / không

ADR (Architecture Decision Record) nhẹ: ghi lại **một** quyết định quan trọng. Nguyên tắc: **AI đề xuất, dev quyết & chịu trách nhiệm.**

---

## 1. Bối cảnh (Context)

`<Vấn đề cần quyết là gì? Vì sao phải quyết bây giờ? Ràng buộc nào?>`

## 2. Phương án đã cân nhắc (≥2)

| # | Phương án | Ưu điểm | Nhược điểm / Trade-off |
|---|---|---|---|
| A | `<...>` | `<...>` | `<...>` |
| B | `<...>` | `<...>` | `<...>` |

## 3. Rủi ro & Ảnh hưởng (Risk / Impact)

- **Rủi ro:** `<điều có thể hỏng + xác suất/mức độ>`
- **Ảnh hưởng cross-cutting:** `<module nào khác, cache, migration, contract, backward-compat>`
- **Giả định cần verify:** `<đánh dấu điều chưa chắc — phải kiểm chứng bằng đọc code/chạy test>`

## 4. Khuyến nghị của AI Agent

`<AI khuyến nghị phương án nào + lý do; được phép bất đồng có lập luận với dev>`

## 5. Quyết định của Dev

- **Chọn:** `<Phương án X>`
- **Lý do:** `<vì sao dev chọn — có thể khác khuyến nghị của AI>`
- **Đã hiểu & chịu trách nhiệm:** ☑ (dev xác nhận không merge code mình không hiểu)

## 6. Hệ quả (Consequences)

`<Sau khi chọn thì phải làm gì tiếp, nợ kỹ thuật phát sinh, việc cần theo dõi>`
