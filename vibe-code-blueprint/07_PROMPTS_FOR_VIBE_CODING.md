# 🤖 07. PROMPTS FOR VIBE CODING PLAYBOOK
> **Bộ Prompt Chuẩn Để Copy & Paste Cho AI Agent (Cursor, Windsurf, Claude Code, Antigravity)**  
> *Được chia thành 5 giai đoạn tuần tự để tránh tràn context và đảm bảo độ chính xác 99%.*

---

## 💡 NGUYÊN TẮC VIBE CODING THÀNH CÔNG
1. **Không yêu cầu AI làm hết trong 1 câu lệnh**: Hãy đi theo từng Phase từ 1 đến 5.
2. **Kèm theo ngữ cảnh**: Khi dùng Cursor / Windsurf, gõ dấu `@` và đính kèm file đặc tả tương ứng trong thư mục `vibe-code-blueprint/`.
3. **Kiểm thử ngay sau mỗi Phase**: Sau mỗi Phase, chạy lệnh kiểm tra xem container hay service có chạy ổn không rồi mới sang Phase tiếp theo.

---

## 🚀 PHASE 1: DOCKER INFRASTRUCTURE & POSTGRESQL DATABASE

### Prompt Copy-Paste:
```text
Tôi đang xây dựng một nền tảng DMS Edge AI + IoT Fleet Safety Core có tên là DriverGuard.
Hãy đọc kỹ file @vibe-code-blueprint/01_ARCHITECTURE_AND_INFRA.md và @vibe-code-blueprint/02_DATABASE_SCHEMA_POSTGRES.md.

Yêu cầu thực hiện:
1. Tạo file `docker-compose.yml` triển khai 4 dịch vụ:
   - PostgreSQL 15 (cổng container 5432, có healthcheck)
   - EMQX Broker 5.3.2 (cổng 1883, 8083, 18083)
   - MinIO Object Storage (cổng 9000, 9001)
   - FastAPI Backend (build từ Dockerfile)
2. Tạo file `docker-compose.override.yml` để map cổng Postgres ra host là `5433:5432` (tránh xung đột port 5432 trên máy host).
3. Tạo file `.env.example` và `.env` đầy đủ biến môi trường.
4. Tạo cấu trúc thư mục backend chuẩn FastAPI:
   - `src/core/` (config, security)
   - `src/database.py` (SQLAlchemy 2.0 Async Session)
   - `src/models/` (User, Vehicle, ShiftSession, SafetyAlertLog, HitlReviewCase, DeviceHeartbeat)
   - `src/seed.py` (Script khởi tạo sẵn 4 user admin, manager, driver1, ngatai và 3 xe test)
5. Tạo `Dockerfile` cho backend Python 3.11-slim, lưu ý cài đặt vào `/usr/local` để tránh lỗi quyền user.
```

---

## 🚀 PHASE 2: REST API CORE (AUTH, FLEET, SHIFT LIFECYCLE)

### Prompt Copy-Paste:
```text
Tiếp tục dự án DriverGuard. Hãy đọc file @vibe-code-blueprint/03_API_REST_AND_SSE_SPEC.md.

Yêu cầu thực hiện:
1. Triển khai phân hệ xác thực:
   - `POST /api/v1/auth/login` (Xác thực JWT HS256, trả về token và user info)
   - `GET /api/v1/auth/me` (Lấy thông tin tài khoản hiện tại từ Bearer Token)
   - Phân quyền RBAC (Role: ADMIN, MANAGER, DRIVER)
2. Triển khai phân hệ quản lý Đội xe & Ca lái:
   - `GET /api/v1/vehicles` (Lọc xe theo trạng thái AVAILABLE, IN_USE)
   - `POST /api/v1/vehicles/{vehicle_id}/assign-driver` (Điều phối viên phân công xe cho tài xế)
   - `POST /api/v1/shifts/start` (Check-in ca lái tự động: Backend tự động map xe đã phân công cho tài xế `current_user.assigned_vehicle_id`, kiểm tra xe có đang bận không, tạo ShiftSession status ACTIVE, trả về thông tin cấu hình MQTT gồm broker_endpoint, client_id kèm random suffix, topics)
   - `POST /api/v1/shifts/end` (Check-out ca lái: cập nhật odometer, giải phóng xe về AVAILABLE)
   - `GET /api/v1/shifts/active` (Lấy ca lái đang chạy của tài xế)
3. Viết Pydantic schemas xác thực request/response chặt chẽ trong `src/schemas/`. Cập nhật schema LoginResponse để trả về kèm `assigned_vehicle`.
```

---

## 🚀 PHASE 3: MQTT INGESTION WORKER, S3 STORAGE & SSE STREAM

### Prompt Copy-Paste:
```text
Tiếp tục dự án DriverGuard. Hãy đọc file @vibe-code-blueprint/04_MQTT_MESSAGING_SPEC.md và mục SSE trong @vibe-code-blueprint/03_API_REST_AND_SSE_SPEC.md.

Yêu cầu thực hiện:
1. Tạo module `src/services/sse_broadcaster.py`:
   - Dùng `asyncio.Queue` để quản lý danh sách kết nối SSE.
   - Hàm `broadcast(event, data)` để đẩy tin nhắn tới toàn bộ client đang mở.
2. Tạo endpoint `GET /api/v1/realtime/stream` trả về `StreamingResponse` với `media_type="text/event-stream"`. Có gửi ping heartbeat định kỳ mỗi 15s.
3. Tạo background service `src/services/mqtt_service.py`:
   - Kết nối tới EMQX Broker khi FastAPI khởi động (trong `lifespan` của `main.py`).
   - Subscribe topic `driverguard/#`.
   - Khi nhận gói tin cảnh báo từ `driverguard/alerts/...`:
     + Lưu vào bảng `SafetyAlertLog`.
     + Nếu severity là HIGH hoặc CRITICAL, tự động tạo 1 case trong `HitlReviewCase` (status PENDING).
     + Gọi `sse_broadcaster.broadcast("alert", payload)`.
   - Khi nhận gói tin GPS: phát sóng qua `sse_broadcaster.broadcast("gps", payload)`.
4. Tạo API lưu trữ hình ảnh qua MinIO S3:
   - `POST /api/v1/media/presigned` (Sinh Presigned URL PUT cho client upload ảnh snapshot lên bucket `driverguard-media`).
5. Tạo API duyệt vi phạm HITL:
   - `GET /api/v1/hitl/queue` (Lấy danh sách vi phạm đang chờ duyệt)
   - `POST /api/v1/hitl/{id}/review` (Gửi quyết định APPROVED hoặc REJECTED kèm ghi chú)
```

---

## 🚀 PHASE 4: ANDROID MOBILE EDGE AI & OFFLINE-FIRST DMS

### Prompt Copy-Paste:
```text
Tôi muốn xây dựng ứng dụng Android cho DriverGuard bằng Java/Kotlin và MediaPipe Face Mesh.
Hãy đọc kỹ file @vibe-code-blueprint/05_EDGE_AI_COMPUTER_VISION.md và @vibe-code-blueprint/02_DATABASE_SCHEMA_POSTGRES.md (phần SQLite Outbox).

Yêu cầu thực hiện:
1. Thiết lập CameraX phân tích khung hình 640x480 @ 30fps bằng `ImageAnalysis.Analyzer`.
2. Tích hợp MediaPipe Face Mesh (468 điểm mốc):
   - Tạo class `FaceFeatureExtractor`:
     + Tính EAR (Eye Aspect Ratio) cho 2 mắt bằng công thức Euclid từ các landmark mí mắt.
     + Tính MAR (Mouth Aspect Ratio) đo độ mở miệng.
     + Ước lượng góc nghiêng đầu Pitch, Yaw bằng PnP hoặc tỷ lệ hình học.
     + Duy trì cửa sổ trượt 60 giây để tính chỉ số PERCLOS.
3. Tạo `DmsDecisionEngine` (Finite State Machine):
   - Nếu EAR < 0.22 kéo dài >= 2.0s -> Báo động DROWSINESS.
   - Nếu MAR > 0.65 kéo dài >= 1.8s -> Báo động YAWN.
   - Nếu |Yaw| > 25 độ hoặc Pitch < -15 độ kéo dài >= 2.5s -> Báo động DISTRACTION.
   - Áp dụng Cooldown 3s sau khi báo động để tránh còi hú liên tục.
4. Xử lý phản hồi âm thanh:
   - Tích hợp Android `TextToSpeech` phát giọng đọc tiếng Việt: "Cảnh báo, vui lòng tập trung lái xe!".
5. Cực kỳ quan trọng (Tránh lỗi mất cảnh báo khi test trong nhà):
   - Viết hàm `getSafeLocation()`: Nếu GPS vệ tinh bị null khi ở phòng kín, phải lấy fallback từ Last Known Location hoặc tọa độ mặc định, TUYỆT ĐỐI không hủy bỏ gói tin cảnh báo.
6. Thiết lập SQLite Outbox & MQTT Client:
   - Tạo bảng `offline_alerts_outbox` để lưu gói tin khi mất sóng 4G.
   - Dùng Paho MQTT v5 với Client ID có kèm chuỗi ngẫu nhiên để tránh đứt kết nối (Flapping).
   - Khi có kết nối lại, background worker tự động quét outbox và đẩy lên EMQX với QoS 1.
```

---

## 🚀 PHASE 5: REACT WEB DASHBOARD & HITL REALTIME OPS

### Prompt Copy-Paste:
```text
Tôi muốn xây dựng giao diện Web Quản trị & Điều hành An toàn Hạm đội bằng React 18, Vite và Lucide Icons.
Hãy đọc kỹ file @vibe-code-blueprint/06_WEB_DASHBOARD_SPEC.md và @vibe-code-blueprint/03_API_REST_AND_SSE_SPEC.md.

Yêu cầu thực hiện:
1. Thiết lập giao diện phong cách Dark Command Center (nền #0B0F19, thẻ kính mờ glassmorphism).
2. Tạo custom hook `useRealtimeAlerts`:
   - Kết nối tới `GET /api/v1/realtime/stream` qua Server-Sent Events.
   - Tự động kết nối lại khi mất mạng.
   - Phát âm thanh báo động còi hú khi nhận sự kiện severity là CRITICAL hoặc HIGH.
3. Tạo màn hình Tổng quan Hạm đội (Fleet Live Map):
   - Tích hợp Leaflet Map hiển thị vị trí các xe đang chạy.
   - Marker đổi màu theo trạng thái tài xế (Xanh: Bình thường, Vàng: Mệt mỏi, Đỏ chớp tắt: Ngủ gật/Nguy hiểm).
4. Tạo Cổng duyệt vi phạm HITL (Human-in-the-Loop Review Center):
   - Danh sách vi phạm đang ở trạng thái PENDING.
   - Khung xem ảnh bằng chứng snapshot từ MinIO S3.
   - Hiển thị chỉ số EAR, tốc độ xe, thời gian vi phạm.
   - Phím tắt bàn phím: bấm phím `A` để Xác nhận vi phạm (APPROVED), bấm phím `R` để Bác bỏ sai số AI (REJECTED).
```
