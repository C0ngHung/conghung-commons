# ADR-001: Wiring `GlobalExceptionHandler` bằng Auto-Configuration thay vì component-scan

> **Ngày:** 2026-07-07 · **Trạng thái:** ✅ ACCEPTED
> **Feature:** `PROJ-9` (G1) · **Người quyết định (dev):** dev repo commons
> **AI Agent đề xuất:** có

---

## 1. Bối cảnh (Context)

`GlobalExceptionHandler` (`@RestControllerAdvice`) hiện chỉ load được vì mọi microservice đặt main class ở `package vn.conghung`, còn lib ở `vn.conghung.common.*` → `@SpringBootApplication` component-scan **tình cờ** với tới. Consumer đặt ở package khác (vd `com.acme`) sẽ **âm thầm mất** handler → rơi về HTTP 500 (sự cố TS-001). Cần một cơ chế để lib tự đăng ký handler, không phụ thuộc package root.

## 2. Phương án đã cân nhắc (≥2)

| # | Phương án | Ưu điểm | Nhược điểm / Trade-off |
|---|---|---|---|
| A | **`@AutoConfiguration` + `AutoConfiguration.imports`** | Chuẩn ngành cho starter; handler load chỉ nhờ dependency; portable; không đụng consumer | Thêm 1 dependency `spring-boot-autoconfigure`; thêm 2 file |
| B | Yêu cầu consumer khai `@Import(GlobalExceptionHandler.class)` hoặc `scanBasePackages="vn.conghung.common"` | Không thêm dependency vào lib | Đẩy gánh nặng sang consumer; dễ quên; TS-001 đã kết luận đây là cách **không nên** |
| C | Giữ nguyên: "chuẩn hoá package root `vn.conghung` cho mọi service" | Không phải sửa lib | Coupling ẩn; service tương lai vẫn dễ dính regression im lặng; không portable |

## 3. Rủi ro & Ảnh hưởng (Risk / Impact)

- **Rủi ro:** thấp — chỉ **thêm** bean qua auto-config, không đổi hành vi consumer hiện có (họ vẫn đang scan được handler).
- **Ảnh hưởng cross-cutting:** mọi service consume lib; sau khi có auto-config, service **không cần** dựa vào package root nữa (dọn coupling ẩn dần).
- **Giả định cần verify:** `spring-boot-autoconfigure:4.0.6` có trong m2 và dùng được scope `provided`. → **Đã verify** (có 4.0.6).

## 4. Khuyến nghị của AI Agent

Chọn **A**. Đây là chuẩn ngành cho `*-spring-boot-starter`, khớp đúng kết luận TS-001, và là fix gốc cho G1 (điểm trừ chính của audit). B/C chỉ vá triệu chứng và duy trì coupling ẩn.

## 5. Quyết định của Dev

- **Chọn:** Phương án **A** — `@AutoConfiguration` + `AutoConfiguration.imports`.
- **Lý do:** khôi phục tính portable, đúng chuẩn starter, không breaking, chi phí thấp.
- **Đã hiểu & chịu trách nhiệm:** ☑

## 6. Hệ quả (Consequences)

- Thêm dependency `spring-boot-autoconfigure` (`provided`) + class `CommonsAutoConfiguration` + resource `.imports`.
- Test bằng `ApplicationContextRunner` để chứng minh load độc lập package (AC1).
- Về sau có thể mở rộng auto-config cho các bean hạ tầng khác (liên quan G3).
