# ADR-002: Đổi `ApiResult.requestDateTime` từ ICT (Asia/Ho_Chi_Minh) sang UTC

> **Ngày:** 2026-07-07 · **Trạng thái:** ✅ ACCEPTED
> **Feature:** `PROJ-9` (G2a) · **Người quyết định (dev):** dev repo commons
> **AI Agent đề xuất:** có

---

## 1. Bối cảnh (Context)

`ApiResult` đóng dấu `requestDateTime` theo `Asia/Ho_Chi_Minh` (ICT) trong mọi factory. Nhưng có handler ở order-service dựng envelope bằng constructor thô với `OffsetDateTime.now(ZoneOffset.UTC)`. Kết quả: cùng field `requestDateTime` trên cùng API surface lại có **2 zone** (success = ICT, một số error = UTC) → không nhất quán, khó đối soát log giữa các service.

## 2. Phương án đã cân nhắc (≥2)

| # | Phương án | Ưu điểm | Nhược điểm / Trade-off |
|---|---|---|---|
| A | **Chuẩn hoá tất cả về UTC** (`ZoneOffset.UTC`) | Chuẩn ngành cho timestamp máy-đọc; nhất quán toàn fleet; dễ đối soát | **Breaking** với client đang parse local time; cần rollout đồng bộ service |
| B | Giữ ICT, ép order-service cũng dùng ICT | Không breaking client hiện tại | Vẫn "sai chuẩn" (timestamp nên trung tính zone); đẩy debt sang service |
| C | Inject `Clock` + config zone | Linh hoạt, testable | Factory `ApiResult` là **static** → phải redesign sang bean có state; over-engineering cho nhu cầu hiện tại |

## 3. Rủi ro & Ảnh hưởng (Risk / Impact)

- **Rủi ro:** client (web/mobile) đang hiển thị `requestDateTime` như local time sẽ lệch +7h nếu không cập nhật.
- **Ảnh hưởng cross-cutting:** mọi consumer của `ApiResult`; cần đồng bộ với account/product/order (repo microservice — ngoài scope repo này, nhưng phải phối hợp rollout).
- **Giả định cần verify:** không test nào hard-code offset ICT ngoài `ApiResultTest`. → verify khi làm P3.

## 4. Khuyến nghị của AI Agent

Chọn **A** (UTC), **không** kèm `Clock` lúc này. UTC là chuẩn cho timestamp trong payload; `Clock` injection (C) đòi redesign static factory → chi phí lớn hơn lợi ích ở thời điểm này, ghi nhận làm nợ kỹ thuật nếu sau này cần test thời gian tất định.

## 5. Quyết định của Dev

- **Chọn:** Phương án **A** — `ZoneOffset.UTC`, hoãn `Clock`.
- **Lý do:** đạt nhất quán ngay với chi phí thấp; chấp nhận breaking có kiểm soát (đánh `feat!`, ghi changelog, phối hợp bump service).
- **Đã hiểu & chịu trách nhiệm:** ☑ (dev nắm đây là breaking change cần điều phối rollout)

## 6. Hệ quả (Consequences)

- Commit dạng `feat(api)!:` + footer `BREAKING CHANGE`.
- Cập nhật `ApiResultTest` assert offset UTC.
- Ghi note README timezone; nhắc service (repo microservice, G2b) đi qua factory + UTC để hết lệch.
