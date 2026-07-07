# Plan: <TÊN FEATURE / TICKET>

> **ID:** `<PROJ-N>` · **Spec:** `docs/specs/<PROJ-N>.spec.md` · **Ngày:** `<YYYY-MM-DD>`
> **Loại:** plan = **HOW**. Bám theo `spec.md`; mọi quyết định lớn ghi ADR.

---

## 1. Chiến lược tổng thể

- **Branch:** `<feature|bugfix>/<PROJ-N>-<slug>` (theo convention repo)
- **Cách chia:** Phase = **Bounded Context** (gom theo vùng nghiệp vụ/kỹ thuật). Mỗi phase → nhiều step. Mỗi step = nhỏ nhất verify được + 1 Conventional Commit.
- **Điểm dừng:** `<local commits | push + PR>`

## 2. Bounded Context → Phase

| Phase (Bounded Context) | Concern | Yêu cầu/Gap phủ | Breaking? |
|---|---|---|---|
| P1 — `<tên BC>` | `<nó chịu trách nhiệm gì>` | `<R1, R2>` | Không/Có |

> **Vì sao gom như vậy:** `<lý do — cùng chạm 1 vùng contract, cùng file lõi, cùng rollout>`

## 3. Chi tiết từng Phase

### P1 — `<tên bounded context>`
- **Mục tiêu:** `<...>`
- **Cách tiếp cận:** `<mô tả kỹ thuật; tái dùng gì có sẵn — kèm file path>`
- **Step:** liệt kê ở `tasks.md`.
- **Rủi ro/Quyết định:** link `docs/decisions/ADR-*.md`.

## 4. Tái sử dụng (đừng viết lại)

- `<hàm/tiện ích/pattern đã có + file path>`

## 5. Verify tổng thể (end-to-end)

- `<lệnh chạy + điều quan sát để khẳng định feature hoạt động thật, không chỉ pass test>`

## 6. Ghi chú rủi ro

- `<điểm dễ sai nhất, giả định cần kiểm chứng>`
