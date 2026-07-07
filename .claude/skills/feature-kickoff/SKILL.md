---
name: feature-kickoff
description: Khởi động một feature/task từ SRS (audit/ticket/yêu cầu) theo quy trình spec-driven + agentic. Dùng khi bắt đầu một feature mới, cần biến yêu cầu thô thành spec.md → plan.md → tasks.md rồi vào plan mode. Kích hoạt khi user gõ /feature-kickoff hoặc nói "bắt đầu feature", "kickoff", "lên spec/plan cho task này".
---

# Feature Kickoff — spec-driven + agentic

Mục tiêu: biến một **SRS** (audit/ticket/mô tả) thành bộ artifact chuẩn rồi vào plan mode, KHÔNG code vội.

## Quy trình (theo thứ tự, human-in-the-loop)

1. **Đọc & xác nhận SRS.** Nếu user chỉ tới file (vd `troubleshooting/TS-018-*.md`) → đọc hết. Nếu yêu cầu mơ hồ → hỏi 2–3 câu làm rõ (AskUserQuestion) TRƯỚC khi viết gì.
2. **Explore trước, kết luận sau.** Dùng subagent `Explore` (song song, tối đa 3) để nắm code liên quan; đừng đọc tràn lan làm phình context. Chỉ giữ kết luận + file path.
3. **Sinh `spec.md`** (WHAT + WHY) theo template [docs/specs/_TEMPLATE.spec.md](../../../docs/specs/_TEMPLATE.spec.md): vấn đề, scope (trong/ngoài), yêu cầu truy vết được, **acceptance criteria verify được**. Lưu `docs/specs/<PROJ-N>.spec.md`. Xin user duyệt.
4. **Sinh `plan.md`** (HOW) theo [docs/plans/_TEMPLATE.plan.md](../../../docs/plans/_TEMPLATE.plan.md): chia **Phase = Bounded Context**, mỗi phase → step, nêu tái sử dụng (file path). Quyết định lớn → ghi ADR ([docs/decisions/_TEMPLATE.adr.md](../../../docs/decisions/_TEMPLATE.adr.md)).
5. **Sinh `tasks.md`** theo mẫu [docs/tasks/PROJ-9-ts018.tasks.md](../../../docs/tasks/PROJ-9-ts018.tasks.md): bảng step + **test + verify + Conventional Commit + trạng thái** cho từng step.
6. **Trước mọi quyết định kỹ thuật lớn:** chạy `/risk-check` (nêu Risk/Impact/Trade-off + ≥2 phương án + khuyến nghị) → dev quyết → ghi ADR.
7. **Vào plan mode** (EnterPlanMode) trình bày kế hoạch, chờ user duyệt. KHÔNG thực thi code cho tới khi có `tasks.md` được duyệt.

## Nguyên tắc bắt buộc

- **Verify để tin:** mọi giả định (API tồn tại? version nào? Jackson mấy?) phải kiểm chứng bằng đọc code/chạy lệnh trước khi ghi vào spec/plan. Đánh dấu giả định chưa verify.
- **Nhỏ & verify được:** mỗi step phải chạy test/verify độc lập được.
- **Không commit khi chưa được yêu cầu.**
- **Ghi memory** những quyết định phi-hiển-nhiên để phiên sau không phải suy lại.

## Output

3 file `docs/specs|plans|tasks/<PROJ-N>.*` (+ ADR nếu có) + 1 kế hoạch trình qua plan mode. Sẵn sàng để thực thi step-by-step theo `tasks.md`.
