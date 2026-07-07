---
name: java-verify
description: Verify một thay đổi trong repo Maven conghung-commons (Java 21 / Spring Boot 4.0.6) — chạy đúng chuỗi lệnh test/build, theo TDD, và xác nhận hành vi thật trước khi nói "xong". Dùng sau mỗi step code, trước mỗi commit. Kích hoạt khi user gõ /java-verify hoặc nói "verify", "kiểm chứng", "chạy test".
---

# Java Verify — repo conghung-commons

Repo là thư viện Maven (Java 21, Spring Boot parent 4.0.6, JUnit 5 + Mockito, JaCoCo, SpotBugs/FindSecBugs, OWASP dependency-check). CI chạy `mvn clean verify` + SonarCloud gate.

## Chuỗi lệnh theo mức độ

| Khi nào | Lệnh | Ghi chú |
|---|---|---|
| Trong lúc code 1 class | `./mvnw test -Dtest=<TênTest>` | Nhanh nhất, vòng lặp TDD |
| Sau mỗi step | `./mvnw test` | Toàn bộ unit/contract test |
| Ranh giới phase / trước commit lớn | `./mvnw clean verify -Ddependency-check.skip=true` | Gồm SpotBugs + FindSecBugs + JaCoCo + source/javadoc jar. **Skip OWASP** vì cần `NVD_API_KEY` + mạng |
| Trước push (nếu có NVD key) | `./mvnw clean verify` | Full, giống CI |

## Quy tắc

1. **TDD cho logic tiền:** viết/sửa test TRƯỚC hoặc CÙNG code; đỏ → xanh → refactor.
2. **Test style của repo:** JUnit 5 + Mockito thủ công (không `@ExtendWith`), assertions **thuần JUnit** (không AssertJ) — giữ nhất quán. Contract test dùng standalone `MockMvcBuilders.standaloneSetup(...).setControllerAdvice(...)`.
3. **Verify hành vi, không chỉ pass test:** với thay đổi có mặt runtime (vd handler, envelope), dựng input thật (post JSON, dựng exception thật) và quan sát output — đừng tin suông.
4. **Chống hallucination:** nghi ngờ API/exception/version → mở file hoặc chạy `./mvnw dependency:tree` để xác minh (vd Jackson 3 = `tools.jackson.databind.exc.*`).
5. **Definition of "xong":** code + test xanh + `./mvnw test` xanh + (nếu đủ điều kiện) `./mvnw clean verify -Ddependency-check.skip=true` xanh. Chỉ khi đó mới báo hoàn tất.

## Lưu ý
- Có **Stop hook** trong [.claude/settings.json](../../settings.json) tự chạy `./mvnw -q test` khi có thay đổi `.java`/`pom.xml` và **chặn kết thúc** nếu test đỏ. Muốn nhẹ hơn: đổi matcher hoặc tạm gỡ hook.
