# Local DevSecOps Engineering & Security Analysis Guide

> **Mục đích:** Phân tích chất lượng mã nguồn (Static Code Analysis), dò quét lỗ hổng bảo mật mức bytecode (SpotBugs + FindSecBugs) và tự động rà soát lỗ hổng thư viện bên thứ ba (OWASP Dependency-Check) cho toàn bộ hệ thống microservices thuộc Omni-Bank Messaging Hub.
> **Yêu cầu:** Đã cài đặt Docker Desktop và cấu hình Java 21, Maven 3.9+ cục bộ.

---

## 📋 Mục lục

1. [Kiến trúc & Sơ đồ Tích hợp](#kiến-trúc--sơ-đồ-tích-hợp)
2. [Hạ tầng SonarQube Server](#hạ-tầng-sonarqube-server)
3. [Phân tích Bảo mật Bytecode (SpotBugs + FindSecBugs)](#phân-tích-bảo-mật-bytecode-spotbugs--findsecbugs)
4. [Quét Thư viện Bên thứ 3 (OWASP Dependency-Check)](#quét-thư-viện-bên-thứ-3-owasp-dependency-check)
5. [Cách đọc Báo cáo & Phân loại CVE (CVE Triage)](#cách-đọc-báo-cáo--phân-loại-cve-cve-triage)
6. [Quy trình Quét Toàn diện 3-trong-1 (All-in-One Local Scan)](#quy-trình-quét-toàn-diện-3-trong-1-all-in-one-local-scan)
7. [Tích hợp SonarQube for IDE (Connected Mode)](#tích-hợp-sonarqube-for-ide-connected-mode)
8. [Bảo mật Credentials & Token trên Máy cục bộ](#bảo-mật-credentials--token-trên-máy-cục-bộ)
9. [Troubleshooting & Câu hỏi thường gặp](#troubleshooting--câu-hỏi-thường-gặp)

---

## Kiến trúc & Sơ đồ Tích hợp

Mô hình **Triple Guard (Bảo vệ ba lớp)** được áp dụng để chốt chặn lỗi từ giai đoạn lập trình (Shift-Left):
*   **Chốt chặn 1 — IDE (Real-time):** SonarLint Connected Mode chạy trực tiếp trong IntelliJ IDEA/VS Code phát hiện lỗi thời gian thực ngay khi gõ phím → chặn 90% lỗi từ trứng nước.
*   **Chốt chặn 2 — Server (CI/CD):** SonarQube CE + SpotBugs + FindSecBugs quét lại trên CI/CD pipeline khi tạo Pull Request → chặn 10% lỗi lọt lưới, enforce Quality Gate.
*   **Chốt chặn 3 — Library Scan (Build-time):** OWASP Dependency-Check quét CVE thư viện bên thứ 3 mỗi lần build → chặn lỗ hổng supply chain attack.

```
┌───────────────────────────────── omnibank-net ──────────────────────────────────┐
│                                                                                 │
│   ┌─────────────┐     ┌─────────────┐     ┌──────────────────────────────────┐  │
│   │  oracle-db  │     │  rabbitmq   │     │  sonarqube (CE)                  │  │
│   │  :1521      │     │  :5672      │     │  :9000 (host: 9001)              │  │
│   └─────────────┘     └─────────────┘     │                                  │  │
│                                           │  - Nhận báo cáo SonarQube        │  │
│                                           │  - Nhận báo cáo SpotBugs XML     │  │
│                                           │  - Nhận báo cáo OWASP JSON       │  │
│                                           │                                  │  │
│                                           │  depends_on ↓                    │  │
│                                           │  sonarqube-db (PostgreSQL 16)    │  │
│                                           │  :5432 (Internal only)           │  │
│                                           └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Hạ tầng SonarQube Server

### 1. Cấu hình Docker Compose (`docker-compose-sonarqube.yml`)

Để chạy SonarQube Server cùng cơ sở dữ liệu PostgreSQL 16 độc lập, chúng ta sử dụng file `docker-compose-sonarqube.yml` nằm ở thư mục gốc của dự án. Nội dung file cấu hình như sau:

```yaml
services:
  sonarqube-db:
    image: postgres:16-alpine
    container_name: omnibank-sonarqube-db
    restart: unless-stopped
    environment:
      POSTGRES_USER: sonar
      POSTGRES_PASSWORD: sonar_secret
      POSTGRES_DB: sonarqube
    volumes:
      - sonarqube_db:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U sonar -d sonarqube"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - omnibank-net

  sonarqube:
    image: sonarqube:community
    container_name: omnibank-sonarqube
    restart: unless-stopped
    depends_on:
      sonarqube-db:
        condition: service_healthy
    ports:
      - "9001:9000"
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://omnibank-sonarqube-db:5432/sonarqube
      SONAR_JDBC_USERNAME: sonar
      SONAR_JDBC_PASSWORD: sonar_secret
      SONAR_ES_BOOTSTRAP_CHECKS_DISABLE: "true"
    volumes:
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_logs:/opt/sonarqube/logs
      - sonarqube_extensions:/opt/sonarqube/extensions
    ulimits:
      nofile:
        soft: 65536
        hard: 65536

    deploy:
      resources:
        limits:
          memory: 4g
        reservations:
          memory: 2g
    healthcheck:
      test: ["CMD-SHELL", "curl -sf http://localhost:9000/api/system/status | grep -q '\"status\":\"UP\"'"]
      interval: 30s
      timeout: 10s
      retries: 10
      start_period: 120s
    networks:
      - omnibank-net

volumes:
  sonarqube_db:
  sonarqube_data:
  sonarqube_logs:
  sonarqube_extensions:

networks:
  omnibank-net:
    external: true
    name: omni-bank-messaging-hub_omnibank-net
```

### 2. Khởi động nhanh
SonarQube sử dụng cơ sở dữ liệu PostgreSQL 16 hoàn toàn độc lập với Oracle DB của dự án, đảm bảo an toàn và dễ dàng nâng cấp.

```bash
# Bước 1: Khởi động stack chính của dự án (nếu chưa chạy để tạo network omnibank-net)
docker compose -f docker-compose.yml up -d

# Bước 2: Khởi động SonarQube & PostgreSQL
docker compose -f docker-compose-sonarqube.yml up -d

# Bước 3: Theo dõi quá trình khởi động (mất khoảng 1-2 phút)
docker logs -f omnibank-sonarqube
```

Truy cập giao diện Web UI: `http://localhost:9001` (Tài khoản mặc định: `admin` / `admin` - yêu cầu đổi mật khẩu ở lần đăng nhập đầu tiên).

### 3. Phân biệt 3 loại Token trong SonarQube
Việc chọn sai loại Token là nguyên nhân phổ biến nhất gây ra lỗi `ForbiddenException: Insufficient privileges`.

| Loại Token | Ký tự bắt đầu | Quyền hạn | Ứng dụng phù hợp |
|---|---|---|---|
| **Project Analysis** | `sqp_...` | Chỉ có quyền đẩy báo cáo quét lên một project duy nhất. | Dùng trong Maven local scan hoặc CI/CD pipeline (Rất an toàn). |
| **Global Analysis** | `sqa_...` | Có quyền đẩy báo cáo lên mọi dự án trên server. | Dùng cho CI Server tổng (như Jenkins, GitHub Actions). |
| **User Token** | `squ_...` | Có toàn quyền tương tự tài khoản của bạn (đọc code, lấy rule). | **Bắt buộc dùng cho SonarLint trên IDE** (Connected Mode). |

---

## Phân tích Bảo mật Bytecode (SpotBugs + FindSecBugs)

### 1. Giới thiệu
**SpotBugs** phân tích tĩnh bytecode (file `.class`) để phát hiện các lỗi logic và bug tiềm ẩn. Khi kết hợp với plugin **FindSecBugs**, radar bảo mật sẽ quét sâu hơn để nhận diện **130+ loại lỗ hổng bảo mật chuyên sâu** của thế giới Java (như SQL Injection, Path Traversal, Hardcoded Password, Cryptographic Weakness).

### 2. Cấu hình Maven trong `pom.xml` (parent)
Plugin được tích hợp trực tiếp vào pha `verify` của Maven, tự động chạy khi bạn build dự án:

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.6.6</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <xmlOutput>true</xmlOutput>
        <spotbugsXmlOutputDirectory>${project.build.directory}</spotbugsXmlOutputDirectory>
        <failOnError>false</failOnError> <!-- Report security issues without failing the build -->
        <plugins>
            <plugin>
                <groupId>com.h3xstream.findsecbugs</groupId>
                <artifactId>findsecbugs-plugin</artifactId>
                <version>1.13.0</version>
            </plugin>
        </plugins>
    </configuration>
    <executions>
        <execution>
            <id>spotbugs-analyze</id>
            <phase>verify</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 3. Cách xem kết quả trên SonarQube UI
*   Truy cập `http://localhost:9001` → Mở project → Tab **Issues**.
*   Các lỗi SpotBugs hiển thị với nhãn **`SPOTBUGS`** in đậm ở góc dưới bên phải mỗi thẻ lỗi.
*   Lọc theo **Type = Vulnerability** để chỉ xem các lỗi bảo mật (không bao gồm lỗi code smell).

Lệnh chạy riêng biệt: `mvn spotbugs:check` hoặc `mvn spotbugs:spotbugs`.

---

## Quét Thư viện Bên thứ 3 (OWASP Dependency-Check)

### 1. Giới thiệu
Các dự án Microservices Spring Boot sử dụng rất nhiều thư viện bên thứ 3. **OWASP Dependency-Check** tự động phân tích tất cả file `.jar` phụ thuộc của dự án, đối chiếu mã định danh của chúng với cơ sở dữ liệu lỗ hổng bảo mật quốc gia **NVD (National Vulnerability Database)** để phát hiện các lỗ hổng đã được công bố (CVE).

### 2. Cấu hình Maven trong `pom.xml` (parent)
Plugin tự động đọc biến môi trường `NVD_API_KEY` (cấu hình trong `.env.sonar`) khi chạy, không cần khai báo tường minh trong XML:

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>10.0.4</version>
    <configuration>
        <format>ALL</format>
        <outputDirectory>${project.build.directory}</outputDirectory>
        <autoUpdate>true</autoUpdate>
        <failOnError>false</failOnError> <!-- Don't fail build on CVE, just report -->
        <knownExploitedEnabled>false</knownExploitedEnabled> <!-- Disable CISA KEV to avoid WAF 403 errors from US gov servers -->
    </configuration>
    <executions>
        <execution>
            <phase>verify</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 3. Kết quả sau khi quét
Báo cáo xuất hiện dưới **8 định dạng** trong thư mục `target/` của từng module con:

| File | Mục đích sử dụng |
|---|---|
| `dependency-check-report.html` | **Đọc trực quan trên trình duyệt** (Khuyến nghị chính) |
| `dependency-check-report.json` | Nạp lên SonarQube Server qua plugin |
| `dependency-check-report.xml` | Tích hợp CI/CD tools |
| `dependency-check-report.csv` | Mở bằng Excel để lọc/sắp xếp |
| `dependency-check-report.sarif` | Tích hợp GitHub Advanced Security |
| `dependency-check-junit.xml` | Tích hợp JUnit test runner |
| `dependency-check-gitlab.json` | Tích hợp GitLab Security Dashboard |
| `dependency-check-jenkins.html` | Tích hợp Jenkins HTML Publisher |

### 4. Cách xem kết quả
*   **Cách 1 — File HTML cục bộ (Chi tiết nhất):** Mở trực tiếp file `target/dependency-check-report.html` trên trình duyệt.
    ```powershell
    Start-Process "sell-foreign-service\target\dependency-check-report.html"
    ```
*   **Cách 2 — SonarQube UI:** Mở project → Menu trái → **Extensions** → **Dependency-Check**.
    > ⚠️ **Lưu ý:** Với dự án multi-module Maven, UI SonarQube chỉ render báo cáo ở cấp project gốc (parent pom). Để xem chi tiết từng module, sử dụng file HTML cục bộ.

---

## Cách đọc Báo cáo & Phân loại CVE (CVE Triage)

Đây là phần quan trọng nhất trong quy trình DevSecOps: **Không phải mọi CVE đều cần sửa.** Nhiều CVE là False Positive (nhận nhầm) do cơ chế đối chiếu tên sản phẩm (CPE matching) không hoàn hảo.

### 1. Cách đọc bảng Summary trong báo cáo HTML

| Cột | Ý nghĩa | Cách đọc nhanh |
|---|---|---|
| **Dependency** | Tên file thư viện `.jar` | Thư viện nào đang bị quét |
| **Vulnerability IDs** | Mã CPE (nhận dạng sản phẩm) | Nếu có giá trị = tool đã nhận diện được sản phẩm để đối chiếu CVE |
| **Highest Severity** | Mức độ nguy hiểm cao nhất | **CRITICAL > HIGH > MEDIUM > LOW** |
| **CVE Count** | Số lỗ hổng tìm thấy | **Số > 0 = CẦN xem xét** |
| **Confidence** | Độ tin cậy của kết quả | **Highest** = chắc chắn đúng, **Low** = có thể là False Positive |

### 2. Quy tắc ưu tiên sửa lỗi

```
CVE Count = 0    → ✅ An toàn, bỏ qua hoàn toàn
CVE Count > 0    → ⚠️ Cần xem xét, tiếp tục đánh giá:
  ├── Severity CRITICAL + Confidence Highest → 🔴 BẮT BUỘC FIX NGAY
  ├── Severity HIGH     + Confidence Highest → 🟠 Fix sớm
  ├── Severity MEDIUM   + Confidence Highest → 🟡 Lên kế hoạch fix
  └── Bất kỳ Severity   + Confidence Low    → 🔍 Xác minh trước (có thể False Positive)
```

### 3. Quy trình phân loại CVE (CVE Triage Workflow)

```
CVE được phát hiện bởi Dependency-Check
    │
    ▼
Bước 1: Kiểm tra Confidence Level
    ├── Confidence = Low → Nghi ngờ False Positive → Xác minh thủ công
    └── Confidence = Highest → Tiếp tục đánh giá
    │
    ▼
Bước 2: Đánh giá Exploitability (Khả năng khai thác trong ngữ cảnh)
    ├── CVE áp dụng cho Standalone Server nhưng mình dùng Embedded? → False Positive
    ├── CVE nhận nhầm sản phẩm khác cùng tên? → False Positive
    └── CVE áp dụng đúng ngữ cảnh sử dụng → Real Vulnerability → Fix
    │
    ▼
Bước 3: Hành động
    ├── Real Vulnerability → Nâng cấp phiên bản thư viện
    └── False Positive → Thêm vào suppression.xml
```

### 4. Ví dụ thực tế: Kết quả quét Baseline của dự án (Spring Boot 4.0.6)

| Thư viện | Severity | CVEs | Confidence | Đánh giá | Hành động |
|---|---|---|---|---|---|
| `netty-transport-4.2.12.Final.jar` | **CRITICAL** | 13 | Highest | ✅ **Real** — HTTP Request Smuggling (CVE-2026-42581, CVSS 9.8) | Nâng cấp Netty |
| `netty-codec-protobuf-4.2.12.Final.jar` | **CRITICAL** | 13 | Highest | ✅ **Real** — Cùng bộ Netty | Nâng cấp Netty |
| `tomcat-embed-core-11.0.21.jar` | **CRITICAL** | 7 | Highest | ✅ **Real** — Input Validation bypass (CVE-2026-41293, CVSS 9.8) | Nâng cấp Tomcat |
| `angus-activation-2.0.3.jar` | HIGH | 1 | **Low** | ❌ **False Positive** — Tool nhận nhầm với `angus-mail` | Suppress |
| `hibernate-validator-9.0.1.Final.jar` | MEDIUM | 1 | Highest | ❌ **False Positive** — Tool nhận nhầm với `Nu Html Checker` | Suppress |

> 💡 **Bài học quan trọng:** Dù đang dùng Spring Boot 4.0.6 (bản ổn định mới nhất), vẫn có CVE CRITICAL. Đây là **điều hoàn toàn bình thường** trong thực tế doanh nghiệp. Nhiệm vụ của Security Engineer là phân loại (triage) và xử lý chứ không phải hoảng sợ.

### 5. Xử lý False Positive bằng `suppression.xml`

Tạo file `suppression.xml` tại thư mục gốc dự án:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <!-- angus-activation is NOT angus-mail. CVE-2025-7962 is SMTP Injection in angus-mail only -->
    <suppress>
        <notes>False Positive: CPE match confused angus-activation with angus-mail</notes>
        <packageUrl regex="true">^pkg:maven/org\.eclipse\.angus/angus\-activation@.*$</packageUrl>
        <cve>CVE-2025-7962</cve>
    </suppress>
    <!-- hibernate-validator is NOT Nu Html Checker (validator.nu) -->
    <suppress>
        <notes>False Positive: CPE match confused hibernate-validator with Nu Html Checker</notes>
        <packageUrl regex="true">^pkg:maven/org\.hibernate\.validator/hibernate\-validator@.*$</packageUrl>
        <cve>CVE-2025-15104</cve>
    </suppress>
</suppressions>
```

Liên kết vào `pom.xml` (trong thẻ `<configuration>` của `dependency-check-maven`):
```xml
<suppressionFile>suppression.xml</suppressionFile>
```

### 6. Fix thư viện thật sự bị lỗi

Với các thư viện transitive (kéo về gián tiếp bởi Spring Boot), override phiên bản trong `pom.xml`:
```xml
<properties>
    <netty.version>4.2.13.Final</netty.version>
    <tomcat.version>11.0.22</tomcat.version>
</properties>
```

---

## Quy trình Quét Toàn diện 3-trong-1 (All-in-One Local Scan)

Để tối ưu hóa hiệu suất quét, tránh việc nạp nhiều báo cáo lẻ tẻ, chúng ta sử dụng quy trình **All-in-One**. Một lệnh duy nhất sẽ kích hoạt toàn bộ hệ thống quét và tự động nạp kết quả chéo lên SonarQube Server.

### Bước 1: Khai báo thuộc tính nạp báo cáo (Properties trong `pom.xml`)
Đảm bảo bạn đã khai báo các thẻ sau trong `properties` của parent `pom.xml` để thông báo cho SonarQube nạp kết quả từ các plugin khác:

```xml
<sonar.java.spotbugs.reportPaths>target/spotbugsXml.xml</sonar.java.spotbugs.reportPaths>
<sonar.dependencyCheck.htmlReportPath>target/dependency-check-report.html</sonar.dependencyCheck.htmlReportPath>
<sonar.dependencyCheck.jsonReportPath>target/dependency-check-report.json</sonar.dependencyCheck.jsonReportPath>
```

### Bước 2: Chạy lệnh build và phân tích
Mở terminal và thực hiện lệnh kết hợp (nạp cả Sonar Token và NVD API Key từ file `.env.sonar` bảo mật):

#### Với Windows PowerShell (Khuyến nghị):
```powershell
# 1. Nạp các biến môi trường từ file bảo mật .env.sonar cục bộ
$env:SONAR_TOKEN = (Get-Content .env.sonar | Where-Object { $_ -match 'SONAR_TOKEN' } | ForEach-Object { $_.Split('=')[1].Trim() })
$env:NVD_API_KEY = (Get-Content .env.sonar | Where-Object { $_ -match 'NVD_API_KEY' } | ForEach-Object { $_.Split('=')[1].Trim() })

# 2. Biên dịch, quét lỗ hổng bytecode + thư viện, sau đó tải lên SonarQube Server
mvn clean verify sonar:sonar "-Dsonar.token=$env:SONAR_TOKEN" "-Dsonar.host.url=http://localhost:9001"
```

#### Với Git Bash / macOS / Linux:
```bash
source .env.sonar
export NVD_API_KEY # Đảm bảo biến được export để Maven có thể đọc được
mvn clean verify sonar:sonar -Dsonar.token=$SONAR_TOKEN -Dsonar.host.url=$SONAR_HOST_URL
```

> 💡 **Giải thích cơ chế:**
> *   `mvn clean verify`: Thực hiện biên dịch mã nguồn Java thành `.class` ➔ Chạy SpotBugs phân tích bytecode tạo file `spotbugsXml.xml` ➔ Chạy OWASP Dependency-Check phân tích `.jar` tạo file báo cáo JSON/HTML.
> *   `sonar:sonar`: Khởi chạy bộ quét SonarQube ➔ Thu gom mã nguồn cùng các báo cáo `spotbugsXml.xml` và `dependency-check-report.json` đã sinh ở trên ➔ Nén và gửi một báo cáo tích hợp duy nhất lên SonarQube Server tại `http://localhost:9001`.

---

## Tích hợp SonarQube for IDE (Connected Mode)

SonarQube for IDE (tên cũ là SonarLint) giúp lập trình viên phát hiện lỗi bảo mật ngay khi viết code offline. Khi bật **Connected Mode**, IDE sẽ liên kết với Server tập trung để đồng bộ bộ luật chung của team.

### Hướng dẫn kết nối trên IntelliJ IDEA:
1.  Vào **Settings (Preferences)** ➔ **Plugins** ➔ Tìm kiếm và cài đặt **SonarQube for IDE**.
2.  Mở cài đặt **Tools** ➔ **SonarQube for IDE** ➔ Chọn tab **Connections** ➔ Nhấn nút **+ (Add Connection)**.
3.  Cấu hình thông tin:
    *   **Connection Name:** `Omni-Bank Local`
    *   **Server URL:** `http://localhost:9001`
    *   **Authentication:** Chọn **Token** và dán mã **User Token (`squ_...`)** được tạo từ tài khoản cá nhân trên SonarQube UI (*Tuyệt đối không dùng Project Analysis Token ở bước này*).
4.  Sau khi kết nối thành công, chọn tab **Project Bindings** ➔ Tích chọn **Bind to SonarQube / SonarCloud** ➔ Tìm dự án `omni-bank-messaging-hub` trên Server để liên kết.
5.  Nhấn nút **Update Bindings** để tải bộ luật và các xác nhận False Positives (lỗi ngoại lệ được bỏ qua do Leader đánh dấu) về máy.

---

## Bảo mật Credentials & Token trên Máy cục bộ

Bảo mật tuyệt đối thông tin xác thực để tránh lộ lọt mã nguồn và quyền can thiệp hệ thống:

*   **Không commit Token lên Git:** File cấu hình cá nhân `.env.sonar` phải luôn nằm trong danh sách `.gitignore`.
*   **Thiết lập thời gian hết hạn (Expiration):** Khi tạo các mã `User Token` hoặc `Project Analysis Token` trên web UI, luôn đặt thời hạn (như `30 days` hoặc `90 days`). Tránh dùng `No expiration`.
*   **Vô hiệu hóa tức thì:** Nếu nghi ngờ lộ lọt, truy cập ngay vào giao diện Web UI ➔ **My Account** ➔ **Security** ➔ Click **Revoke** bên cạnh tên token để hủy bỏ quyền lực của token đó ngay lập tức.

---

## Troubleshooting & Câu hỏi thường gặp

### ❌ Lỗi: `ForbiddenException: Insufficient privileges` khi quét hoặc liên kết IDE
*   **Nguyên nhân:** Bạn đang dùng nhầm token. Sử dụng `Project Analysis Token (sqp_...)` cho IDE SonarLint sẽ bị chặn vì IDE cần quyền kéo (read) cấu hình luật ngược từ server về, trong khi token này chỉ có quyền đẩy (push-only).
*   **Cách sửa:** Tạo lại Token mới trên Web UI với loại là **`User Token`** và cập nhật lại cấu hình trên IDE.

### ❌ Lỗi: SpotBugs báo không tìm thấy file class để phân tích
*   **Nguyên nhân:** Lệnh quét chạy trước khi code được biên dịch (ví dụ bạn chỉ chạy `mvn sonar:sonar` mà chưa chạy `mvn compile` hoặc `mvn verify`).
*   **Cách sửa:** Luôn sử dụng lệnh kết hợp `mvn clean verify sonar:sonar` để đảm bảo code đã được biên dịch sạch sẽ trước khi phân tích bytecode.

### ❌ Lỗi: Phân tích OWASP Dependency-Check chạy rất lâu ở lần đầu tiên
*   **Nguyên nhân:** Ở lần đầu tiên kích hoạt, plugin cần tải toàn bộ cơ sở dữ liệu lỗ hổng bảo mật quốc gia (hàng GB dữ liệu CVE) từ server NVD về máy của bạn.
*   **Cách sửa:** Kiên nhẫn chờ đợi hoàn tất trong lần đầu. Các lần quét sau, plugin sẽ chỉ tải bản cập nhật vi mô (delta update) nên tốc độ sẽ cực kỳ nhanh.

### ❌ Làm thế nào để so sánh Before vs After trong buổi Demo mà không mất thời gian quét lại?
*   **Mẹo chuyên nghiệp:** Tạo hai Project song song trên Server SonarQube để trình chiếu trên 2 tab.
    *   **Tab 1 (Before):** Giữ nguyên dự án `Omni-Bank-Messaging-Hub` hiện tại (đầy rẫy lỗi ban đầu).
    *   **Tab 2 (After):** Trên nhánh sửa lỗi `after-scan`, chạy lệnh quét kèm thuộc tính đổi tên dự án:
        ```bash
        mvn clean verify sonar:sonar -Dsonar.projectKey=omni-bank-messaging-hub-after -Dsonar.projectName="Omni-Bank Messaging Hub (After Fix)" "-Dsonar.token=$env:SONAR_TOKEN"
        ```
    *   Bạn sẽ có 2 báo cáo độc lập để so sánh tức thì trước mắt hội đồng/team!

---

## DevSecOps Local Best Practices & Remediation Guidelines

Để quy trình DevSecOps cục bộ mang lại hiệu quả cao nhất mà không làm giảm tốc độ phát triển hàng ngày của lập trình viên, hãy áp dụng các thực hành tốt nhất dưới đây:

### 1. Tối ưu hóa hiệu suất build & quét hàng ngày
Việc quét bảo mật bytecode và tải dữ liệu CVE rất tốn tài nguyên. Khi bạn đang code và cần build nhanh để kiểm thử tính năng (chưa cần phân tích bảo mật), hãy sử dụng các tham số bỏ qua sau:

*   **Tắt cập nhật cơ sở dữ liệu NVD (Tăng tốc đáng kể):**
    Sau lần tải đầu tiên thành công, dữ liệu CVE đã được lưu cục bộ. Khi build hàng ngày, hãy thêm `-DautoUpdate=false` để bỏ qua việc kết nối tải lại CSDL NVD:
    ```bash
    mvn clean install -DskipTests -DautoUpdate=false
    ```
*   **Bỏ qua hoàn toàn quét thư viện (Khi chỉ thay đổi logic code):**
    Nếu không thêm/bớt bất kỳ dependency nào trong `pom.xml`, hãy bỏ qua quét CVE để tiết kiệm thời gian:
    ```bash
    mvn clean install -DskipTests -Ddependency-check.skip=true
    ```
*   **Bỏ qua quét SpotBugs (Khi build nhanh kiểm thử logic):**
    ```bash
    mvn clean install -DskipTests -Dspotbugs.skip=true
    ```

---

### 2. Hướng dẫn sửa các lỗi bảo mật phổ biến (Remediation)

Khi chạy quét, nếu phát hiện lỗi bảo mật, hãy tuân thủ nguyên tắc sửa lỗi chuẩn mực dưới đây thay vì dùng các biện pháp chống chế (như tắt cảnh báo):

#### A. Lỗi SQL Injection (Phát hiện bởi FindSecBugs)
*   **Nguyên nhân:** Cộng chuỗi trực tiếp từ tham số người dùng vào câu lệnh SQL (ví dụ: `String sql = "SELECT * FROM users WHERE username = '" + username + "'";`).
*   **Cách khắc phục:**
    *   Sử dụng **Parameterized Queries (PreparedStatement)** của JDBC.
    *   Nếu dùng Spring Data JPA, sử dụng các câu truy vấn JPQL/HQL truyền tham số an toàn qua `@Param` hoặc dùng Spring Data Query Methods (ví dụ: `findByUsername(String username)`).
    *   Tránh tuyệt đối việc tự viết câu lệnh SQL native cộng chuỗi.

#### B. Lỗi Weak Cryptography (Mã hóa yếu)
*   **Nguyên nhân:** Sử dụng thuật toán băm hoặc mã hóa đã lỗi thời và bị phá vỡ về mặt toán học (như `MD5`, `SHA-1`, `DES`, `Blowfish`).
*   **Cách khắc phục:**
    *   **Băm mật khẩu:** Bắt buộc dùng `bcrypt`, `scrypt` hoặc `Argon2` (Spring Security Crypto cung cấp sẵn).
    *   **Băm kiểm tra tính toàn vẹn (Checksum):** Sử dụng tối thiểu `SHA-256` hoặc `SHA-512`.
    *   **Mã hóa đối xứng:** Sử dụng `AES/GCM/NoPadding` với độ dài khóa tối thiểu `128-bit` (khuyên dùng `256-bit`). Tránh dùng chế độ ECB, nếu dùng CBC phải đảm bảo sử dụng Vector khởi tạo ngẫu nhiên bảo mật (`SecureRandom IV`).

#### C. Lỗi Hardcoded Credentials (Lộ lọt thông tin nhạy cảm)
*   **Nguyên nhân:** Khai báo trực tiếp mật khẩu Database, Token, API Key dưới dạng String cứng trong các lớp Java.
*   **Cách khắc phục:**
    *   Đưa toàn bộ thông tin nhạy cảm ra ngoài file cấu hình `application.yaml` hoặc `application.properties`.
    *   Sử dụng cú pháp nạp động của Spring: `@Value("${app.security.token}")`.
    *   Trong môi trường production, kết hợp nạp biến môi trường từ hệ điều hành hoặc sử dụng giải pháp quản lý bí mật chuyên nghiệp (như HashiCorp Vault, AWS Secrets Manager, Oracle Cloud Infrastructure Vault).

#### D. Lỗi lỗ hổng thư viện bên thứ 3 (OWASP CVE)
*   **Nguyên nhân:** Thư viện sử dụng (ví dụ: một phiên bản cũ của `jackson-databind` hoặc `log4j`) có chứa lỗ hổng bảo mật đã được công bố quốc tế.
*   **Cách khắc phục:**
    *   Mở báo cáo `target/dependency-check-report.html` của module bị cảnh báo.
    *   Xem mã định danh lỗi (ví dụ: `CVE-2023-xxxx`) và phiên bản khuyến nghị an toàn (Patched Version).
    *   Cập nhật thẻ `<version>` của thư viện đó trong `pom.xml` lên phiên bản an toàn mới nhất.
    *   Nếu thư viện đó là dependency gián tiếp (transitive dependency) được kéo về bởi một starter khác, hãy sử dụng thẻ `<dependencyManagement>` trong parent `pom.xml` hoặc thẻ `<exclusion>` để ép phiên bản an toàn.

---

### 3. Quy trình phát triển Shift-Left đề xuất cho Team
Để đạt hiệu quả DevSecOps tối đa, toàn bộ thành viên dự án nên áp dụng quy trình làm việc hàng ngày sau:

```
[Viết Code cục bộ]
        ↓
[SonarLint IDE kiểm tra thời gian thực] ──(Phát hiện lỗi)──> [Sửa lỗi ngay trên IDE]
        ↓ (Không còn lỗi trên IDE)
[Chạy: mvn clean verify -DskipTests] ──(Phát hiện lỗi bytecode/CVE)──> [Sửa trong code/pom.xml]
        ↓ (Build & Quét thành công)
[Tạo Pull Request lên Git]
        ↓
[CI/CD tự động quét tự động kiểm tra lại lần cuối trước khi merge]
```

Áp dụng quy trình này giúp giải quyết **95%** các lỗi chất lượng và bảo mật ngay từ máy lập trình viên, giảm tải tối đa thời gian review code và tránh đổ vỡ hệ thống khi tích hợp!

---

## SonarQube Dependency-Check Plugin (Hiển thị trên Web UI)

Để hiển thị báo cáo OWASP Dependency-Check trực tiếp trên giao diện Web SonarQube (thay vì chỉ đọc file HTML cục bộ):

### Cài đặt Plugin vào Docker Container
```powershell
# 1. Tải plugin từ GitHub Releases
docker exec omnibank-sonarqube curl -L -o /opt/sonarqube/extensions/plugins/sonar-dependency-check-plugin-5.0.0.jar "https://github.com/dependency-check/dependency-check-sonar-plugin/releases/download/5.0.0/sonar-dependency-check-plugin-5.0.0.jar"

# 2. Khởi động lại SonarQube để nạp plugin
docker restart omnibank-sonarqube
```

Sau khi khởi động lại, vào project → Menu trái → **Extensions** → **Dependency-Check** để xem báo cáo tổng hợp.

---
*Tài liệu này được duy trì và cập nhật bởi team Omni-Bank Messaging Hub. Cập nhật cuối cùng: 2026-05-20*
