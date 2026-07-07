---
description: Trước khi code một thay đổi, bắt AI nêu Risk / Impact / Trade-off + ≥2 phương án + khuyến nghị, xuất ra format ADR để dev quyết.
argument-hint: <mô tả thay đổi định làm>
---

# Risk-Check — nêu rủi ro & phương án TRƯỚC khi code

Thay đổi đang cân nhắc: **$ARGUMENTS**

Bạn (AI Agent) là **cộng sự phản biện**, không phải máy sinh code. TUYỆT ĐỐI chưa viết/sửa code trong lượt này. Trước tiên, hãy **kiểm chứng** (đọc code/chạy lệnh read-only) mọi giả định liên quan — đừng suy đoán. Sau đó trả lời đúng cấu trúc ADR sau:

## 1. Bối cảnh
Tóm tắt vấn đề cần quyết & ràng buộc (đã verify từ code, dẫn file:line nếu có).

## 2. Phương án (≥2)
Bảng: Phương án | Ưu điểm | Nhược điểm / Trade-off. Ít nhất 2 lựa chọn thực sự khác nhau.

## 3. Rủi ro & Ảnh hưởng
- Rủi ro (điều có thể hỏng + mức độ).
- Ảnh hưởng cross-cutting: module khác, contract/backward-compat, cache, migration, breaking?
- **Giả định chưa chắc** cần verify thêm (đánh dấu rõ).

## 4. Khuyến nghị của tôi (AI)
Chọn phương án nào + vì sao. Được phép **bất đồng có lập luận** với hướng dev đang nghĩ — nếu thấy dev có thể sai, nói thẳng kèm lý do.

## 5. Câu hỏi cho Dev
Những điểm cần dev quyết (dùng AskUserQuestion nếu là lựa chọn rẽ nhánh).

---
Sau khi dev chốt: ghi lại thành `docs/decisions/ADR-<NNN>-<slug>.md` theo [template](../../docs/decisions/_TEMPLATE.adr.md) rồi mới tiến hành code. Nhớ: **dev quyết & chịu trách nhiệm; không merge code không hiểu.**
