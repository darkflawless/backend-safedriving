# 📡 03. REST API & SSE REALTIME SPECIFICATION
> **Đặc Tả Chi Tiết Các Giao Tiếp HTTP REST API và Server-Sent Events (SSE)**

---

## 1. TỔNG QUAN PHÂN HỆ API

Base URL: `http://<SERVER_HOST>:8000/api/v1`  
Chuẩn xác thực: Bearer Token (JWT) truyền qua header: `Authorization: Bearer <TOKEN>`  
Chuẩn dữ liệu: `Content-Type: application/json`

---

## 2. NHÓM API AUTHENTICATION & IDENTITY

### 2.1 Đăng Nhập Hệ Thống
- **Endpoint**: `POST /auth/login`
- **Quyền hạn**: Public
- **Request Body**:
```json
{
  "email": "driver1@driverguard.ai",
  "password": "Driver@123"
}
```
- **Response `200 OK`**:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "user": {
    "id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "email": "driver1@driverguard.ai",
    "full_name": "Nguyễn Văn Lái Xe",
    "role": "DRIVER",
    "phone": "0987654321",
    "assigned_vehicle": {
      "id": "f8a12345-0000-0000-0000-000000000001",
      "plate_number": "30G-456.78",
      "model": "VinFast VF8",
      "vin": "VF8VN2024001"
    }
  }
}
```
- **Mã lỗi**:
  - `401 Unauthorized`: Sai tài khoản hoặc mật khẩu.
  - `403 Forbidden`: Tài khoản bị khóa (`is_active = false`).

### 2.2 Lấy Thông Tin Người Dùng Hiện Tại
- **Endpoint**: `GET /auth/me`
- **Headers**: `Authorization: Bearer <TOKEN>`
- **Response `200 OK`**: Thông tin người dùng kèm `assigned_vehicle` tương tự như khi login.

---

## 3. NHÓM API QUẢN LÝ XE & CA LÁI (FLEET & SHIFTS)

### 3.1 Điều Phối Phân Công Xe Cho Tài Xế (Dành cho Fleet Manager / Admin)
- **Endpoint**: `POST /vehicles/{vehicle_id}/assign-driver`
- **Headers**: `Authorization: Bearer <MANAGER_TOKEN>`
- **Request Body**:
```json
{
  "driver_id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"
}
```
- **Response `200 OK`**:
```json
{
  "message": "Vehicle assigned successfully",
  "vehicle_id": "f8a12345-0000-0000-0000-000000000001",
  "assigned_driver_id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"
}
```

### 3.2 Bắt Đầu Ca Lái (Shift Check-in Tự Động từ Mobile)
- **Endpoint**: `POST /shifts/start`
- **Headers**: `Authorization: Bearer <DRIVER_TOKEN>`
- **Mô tả**: Mobile **không cần truyền `vehicle_id`** vì Backend tự động xác định phương tiện đã gán sẵn cho tài xế đăng nhập (`current_user.assigned_vehicle_id`).
- **Request Body**:
```json
{
  "start_odometer": 12450.5,
  "start_battery": 85.0
}
```
- **Response `201 Created`**:
```json
{
  "shift_id": "s9999999-1111-2222-3333-444455556666",
  "driver_id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "vehicle": {
    "id": "f8a12345-0000-0000-0000-000000000001",
    "plate_number": "30G-456.78",
    "model": "VinFast VF8"
  },
  "start_time": "2026-10-04T02:30:00Z",
  "status": "ACTIVE",
  "mqtt_config": {
    "broker_endpoint": "tcp://192.168.112.72:1883",
    "client_id": "DRIVERGUARD_f8a12345_s9999999_8f2a",
    "telemetry_topic": "driverguard/telemetry/f8a12345-0000-0000-0000-000000000001/gps",
    "alert_topic": "driverguard/alerts/f8a12345-0000-0000-0000-000000000001",
    "heartbeat_topic": "driverguard/heartbeat/device_xyz"
  }
}
```
- **Mã lỗi**:
  - `400 Bad Request`: "Tài xế chưa được phân công phương tiện. Vui lòng liên hệ Điều phối viên."
  - `409 Conflict`: "Phương tiện hiện đang được sử dụng trong một ca lái khác chưa kết thúc".

### 3.3 Kết Thúc Ca Lái (Shift Check-out)
- **Endpoint**: `POST /shifts/end`
- **Request Body**:
```json
{
  "shift_id": "s9999999-1111-2222-3333-444455556666",
  "end_odometer": 12510.0,
  "end_battery": 72.0
}
```
- **Response `200 OK`**: Cập nhật ca sang `COMPLETED`, giải phóng trạng thái xe về `AVAILABLE`.

---

## 4. NHÓM API LƯU TRỮ HÌNH ẢNH MINIO/S3 (STORAGE)

### 4.1 Xin Presigned URL Để Mobile Tải Ảnh Bằng Chứng Vi Phạm
- **Endpoint**: `POST /media/presigned`
- **Request Body**:
```json
{
  "filename": "alert_snapshot_20261004_023512.jpg",
  "content_type": "image/jpeg",
  "folder": "snapshots"
}
```
- **Response `200 OK`**:
```json
{
  "upload_url": "http://192.168.112.72:9000/driverguard-media/snapshots/alert_snapshot_20261004_023512.jpg?X-Amz-Algorithm=...",
  "public_url": "http://192.168.112.72:9000/driverguard-media/snapshots/alert_snapshot_20261004_023512.jpg",
  "expires_in": 3600
}
```

---

## 5. NHÓM API DUYỆT VI PHẠM (HUMAN-IN-THE-LOOP - HITL)

### 5.1 Lấy Danh Sách Vi Phạm Chờ Duyệt
- **Endpoint**: `GET /hitl/queue?status=PENDING&limit=20&offset=0`
- **Headers**: `Authorization: Bearer <MANAGER_OR_ADMIN_TOKEN>`
- **Response `200 OK`**:
```json
[
  {
    "id": "h7777777-aaaa-bbbb-cccc-ddddffff0001",
    "alert_id": "c5555555-1111-2222-3333-444455556666",
    "status": "PENDING",
    "created_at": "2026-10-04T02:35:15Z",
    "alert_details": {
      "alert_type": "DROWSINESS",
      "severity": "CRITICAL",
      "timestamp": "2026-10-04T02:35:12Z",
      "vehicle_plate": "30G-456.78",
      "driver_name": "Nguyễn Văn Lái Xe",
      "speed_kmh": 65.4,
      "ear_value": 0.14,
      "perclos_value": 0.42,
      "snapshot_url": "http://192.168.112.72:9000/driverguard-media/snapshots/alert_snapshot_20261004_023512.jpg"
    }
  }
]
```

### 5.2 Gửi Quyết Định Duyệt (Approve / Reject)
- **Endpoint**: `POST /hitl/{case_id}/review`
- **Request Body**:
```json
{
  "status": "APPROVED",
  "review_notes": "Tài xế nhắm mắt quá 2.5s khi xe đang chạy 65km/h. Đã gọi điện nhắc nhở ngay lập tức."
}
```
- **Response `200 OK`**:
```json
{
  "message": "Review submitted successfully",
  "case_id": "h7777777-aaaa-bbbb-cccc-ddddffff0001",
  "new_status": "APPROVED"
}
```

---

## 6. ĐẶC TẢ REALTIME STREAM (SERVER-SENT EVENTS - SSE)

- **Endpoint**: `GET /realtime/stream`
- **Headers**:
  - `Accept: text/event-stream`
  - `Cache-Control: no-cache`
  - `Connection: keep-alive`
- **Đặc điểm**: Đẩy trực tiếp mọi cảnh báo và dữ liệu vị trí GPS của toàn bộ đội xe tới Web Frontend mà không cần polling.

### Cấu Trúc Khung Tin (SSE Frame)

#### Frame Sự Kiện Cảnh Báo Nguy Hiểm (`event: alert`)
```http
event: alert
data: {"id":"c5555555-1111-2222-3333-444455556666","vehicle_id":"f8a12345-0000-0000-0000-000000000001","plate_number":"30G-456.78","driver_name":"Nguyễn Văn Lái Xe","alert_type":"DROWSINESS","severity":"CRITICAL","timestamp":"2026-10-04T02:35:12Z","latitude":21.028511,"longitude":105.854444,"speed_kmh":65.4,"ear":0.14,"snapshot_url":"http://192.168.112.72:9000/driverguard-media/snapshots/sample.jpg"}
```

#### Frame Định Vị Xe Thời Gian Thực (`event: gps`)
```http
event: gps
data: {"vehicle_id":"f8a12345-0000-0000-0000-000000000001","plate_number":"30G-456.78","latitude":21.028511,"longitude":105.854444,"speed_kmh":65.4,"heading":180.0,"timestamp":"2026-10-04T02:35:15Z"}
```

#### Frame Giữ Kết Nối Định Kỳ (`event: heartbeat`)
```http
event: ping
data: {"time": 1728000000}
```
*(Được gửi mỗi 15 giây để trình duyệt không tự động ngắt kết nối).*
