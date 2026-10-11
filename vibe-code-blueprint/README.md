# 🚀 DRIVERGUARD VIBE-CODING BLUEPRINT PACK
> **Bộ tài liệu thiết kế & đặc tả hệ thống toàn diện (99% Clone Ready)**  
> Dành cho việc dùng AI Agent (Cursor, Windsurf, Claude Code, Antigravity) để "vibe code" một nền tảng **DMS Edge AI + IoT Fleet Safety Core** tương tự từ đầu đến cuối.

---

## 📂 Danh Mục Tài Liệu Trong Thư Mục Này

Toàn bộ tài liệu được đóng gói độc lập. Bạn có thể **copy toàn bộ thư mục `vibe-code-blueprint`** này sang bất kỳ thư mục hay dự án mới nào:

| File | Mô tả nội dung |
| :--- | :--- |
| [**`00_SYSTEM_BLUEPRINT_MASTER.md`**](./00_SYSTEM_BLUEPRINT_MASTER.md) | **Bản tổng tài liệu tổng hợp (All-in-One Master)**: Toàn bộ kiến trúc, luồng nghiệp vụ, công thức toán, API, MQTT và kịch bản vận hành. |
| [**`01_ARCHITECTURE_AND_INFRA.md`**](./01_ARCHITECTURE_AND_INFRA.md) | **Hạ tầng Docker & Network**: Cấu hình Docker Compose (FastAPI, EMQX, MinIO, PostgreSQL), port mapping, cấu hình mạng Wi-Fi LAN bridge cho thiết bị thật. |
| [**`02_DATABASE_SCHEMA_POSTGRES.md`**](./02_DATABASE_SCHEMA_POSTGRES.md) | **Thiết kế CSDL & Schema**: SQLAlchemy models, PostgreSQL DDL, Enums, Indexes, Foreign keys và Schema SQLite Outbox Offline trên điện thoại. |
| [**`03_API_REST_AND_SSE_SPEC.md`**](./03_API_REST_AND_SSE_SPEC.md) | **Đặc tả REST API & SSE**: Contract chuẩn OpenAPI cho Auth, Ca lái (Shift), Xe (Vehicles), Cảnh báo (Alerts), S3 Presigned Upload và SSE Realtime Stream. |
| [**`04_MQTT_MESSAGING_SPEC.md`**](./04_MQTT_MESSAGING_SPEC.md) | **Giao thức IoT MQTT v5**: Cấu trúc Topic, QoS, Payload JSON mẫu cho Telemetry GPS, Heartbeat, và Safety Alerts từ thiết bị Edge AI lên Server. |
| [**`05_EDGE_AI_COMPUTER_VISION.md`**](./05_EDGE_AI_COMPUTER_VISION.md) | **MediaPipe Edge AI (Android)**: CameraX, Landmark 468 điểm, công thức toán EAR, MAR, Head Pose, PERCLOS, Finite State Machine, Audio TTS và Fallback GPS indoor. |
| [**`06_WEB_DASHBOARD_SPEC.md`**](./06_WEB_DASHBOARD_SPEC.md) | **Giao diện Web Giám Sát (React/Vite)**: Bản đồ hạm đội Live GPS (Leaflet), chuông báo realtime SSE, bảng điều khiển xem xét vi phạm (HITL Review UI). |
| [**`07_PROMPTS_FOR_VIBE_CODING.md`**](./07_PROMPTS_FOR_VIBE_CODING.md) | **Bộ Prompt mẫu chuẩn "Copy & Paste"**: Dùng cho Cursor/Claude/ChatGPT để tạo lần lượt từng module theo từng Phase mà không bị tràn context. |

---

## 🎯 Cách Dùng Để Vibe Code 1 Dự Án Mới
1. Tạo một thư mục repo mới (ví dụ: `my-dms-project/`).
2. Copy toàn bộ thư mục `vibe-code-blueprint/` vào repo đó.
3. Mở Cursor / Windsurf / Antigravity.
4. Mở file [**`07_PROMPTS_FOR_VIBE_CODING.md`**](./07_PROMPTS_FOR_VIBE_CODING.md), copy từng prompt tương ứng từ **Phase 1 -> Phase 5** và gửi cho AI. Kèm theo link `@01_...`, `@02_...` để AI tự đọc context chi tiết.
