# 📨 04. MQTT IOT MESSAGING SPECIFICATION
> **Đặc Tả Giao Thức MQTT v5, Cấu Trúc Topic, Cấp Độ QoS và Định Dạng Dữ Liệu JSON**

---

## 1. NGUYÊN TẮC THIẾT KẾ TOPIC TAXONOMY

Hệ thống DriverGuard sử dụng định danh Topic phân cấp theo chuẩn IoT công nghiệp:

```
driverguard / <domain> / <entity_id> / [sub_type]
```

| Tên Topic Mẫu | Mục đích | Cấp độ QoS | Chu kỳ phát |
| :--- | :--- | :---: | :--- |
| `driverguard/telemetry/{vehicle_id}/gps` | Tọa độ GPS, vận tốc, hướng di chuyển của xe | **QoS 0** | 1 - 3 giây / lần |
| `driverguard/alerts/{vehicle_id}/{alert_type}` | Cảnh báo an toàn (Ngủ gật, Mất tập trung, v.v.) | **QoS 1** (Có xác nhận ACK) | Tức thời khi vi phạm |
| `driverguard/heartbeat/{device_id}` | Trạng thái pin, nhiệt độ, FPS camera của thiết bị | **QoS 0** | 15 - 30 giây / lần |

---

## 2. QUY TẮC ĐẶT CLIENT ID ĐỂ TRÁNH XUNG ĐỘT (AVOID FLAPPING)

Theo chuẩn MQTT, nếu hai Client kết nối đến Broker với **cùng một Client ID**, Broker sẽ ngắt kết nối Client cũ để nhường chỗ cho Client mới.  
**Quy tắc sinh Client ID trên Mobile**:
```text
DRIVERGUARD_<VEHICLE_ID>_<SHIFT_ID>_<4_RANDOM_CHARS>
Ví dụ: DRIVERGUARD_veh123_shift456_a9b1
```
*Điều này đảm bảo dù nhiều điện thoại cùng đăng nhập thử nghiệm, Broker vẫn duy trì kết nối ổn định.*

---

## 3. ĐỊNH DẠNG PAYLOAD CHI TIẾT (JSON SCHEMAS)

### 3.1 Topic Telemetry GPS (`driverguard/telemetry/{vehicle_id}/gps`)
- **QoS**: `0` (Fire and forget, không lưu trữ nếu mất mạng để tránh nghẽn băng thông).
- **Ví dụ Payload**:
```json
{
  "version": "1.0",
  "vehicle_id": "f8a12345-0000-0000-0000-000000000001",
  "shift_id": "s9999999-1111-2222-3333-444455556666",
  "timestamp": 1728000012,
  "gps": {
    "latitude": 21.028511,
    "longitude": 105.854444,
    "speed_kmh": 65.4,
    "heading": 182.5,
    "altitude": 12.0,
    "accuracy_m": 4.5
  },
  "metrics": {
    "engine_running": true,
    "odometer_km": 12455.2
  }
}
```

---

### 3.2 Topic Cảnh Báo An Toàn (`driverguard/alerts/{vehicle_id}/{alert_type}`)
- **QoS**: `1` (At least once - Broker phải gửi lại gói `PUBACK`).
- **Lưu ý**: Nếu thiết bị mất sóng, gói tin này **phải được ghi vào SQLite Outbox** và đẩy lại khi có mạng.
- **Ví dụ Payload**:
```json
{
  "event_id": "c5555555-1111-2222-3333-444455556666",
  "version": "1.0",
  "vehicle_id": "f8a12345-0000-0000-0000-000000000001",
  "shift_id": "s9999999-1111-2222-3333-444455556666",
  "driver_id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "alert_type": "DROWSINESS",
  "severity": "CRITICAL",
  "timestamp": 1728000012,
  "telemetry": {
    "latitude": 21.028511,
    "longitude": 105.854444,
    "speed_kmh": 65.4
  },
  "biometrics": {
    "ear": 0.142,
    "mar": 0.285,
    "pitch_deg": -5.2,
    "yaw_deg": 3.1,
    "perclos_60s": 0.425,
    "consecutive_closed_frames": 62
  },
  "evidence": {
    "snapshot_url": "http://192.168.112.72:9000/driverguard-media/snapshots/alert_snapshot_20261004_023512.jpg",
    "clip_duration_s": 0
  }
}
```

---

### 3.3 Topic Heartbeat Thiết Bị (`driverguard/heartbeat/{device_id}`)
- **QoS**: `0`
- **Ví dụ Payload**:
```json
{
  "device_id": "samsung_galaxy_s21_fe_01",
  "vehicle_id": "f8a12345-0000-0000-0000-000000000001",
  "timestamp": 1728000030,
  "battery_pct": 78,
  "is_charging": true,
  "cpu_temp_c": 38.5,
  "camera_fps": 29.8,
  "storage_free_mb": 14250,
  "app_version": "1.0.4-prod"
}
```

---

## 4. MẪU XỬ LÝ INGESTION WORKER TRÊN FASTAPI

```python
# src/services/mqtt_service.py
import asyncio
import json
import logging
from aiomqtt import Client, MqttError
from src.config import get_settings
from src.database import get_db_context
from src.models.safety import SafetyAlertLog, HitlReviewCase, AlertSeverity
from src.services.sse_broadcaster import sse_broadcaster

logger = logging.getLogger("MQTT_SERVICE")

class MqttIngestionService:
    def __init__(self):
        self.settings = get_settings()
        self._running = False
        self._task = None

    async def start(self):
        self._running = True
        self._task = asyncio.create_task(self._listen_loop())

    async def stop(self):
        self._running = False
        if self._task:
            self._task.cancel()

    async def _listen_loop(self):
        while self._running:
            try:
                async with Client(
                    hostname=self.settings.mqtt_broker_host,
                    port=self.settings.mqtt_broker_port,
                    keepalive=60
                ) as client:
                    logger.info("Connected to EMQX Broker successfully!")
                    # Đăng ký lắng nghe toàn bộ topic trong namespace driverguard
                    await client.subscribe("driverguard/#")

                    async for message in client.messages:
                        await self._process_message(message.topic.value, message.payload.decode())
            except MqttError as e:
                logger.warning(f"MQTT connection lost: {e}. Reconnecting in 3s...")
                await asyncio.sleep(3)
            except Exception as e:
                logger.error(f"Unexpected MQTT worker error: {e}")
                await asyncio.sleep(5)

    async def _process_message(self, topic: str, payload_str: str):
        try:
            data = json.loads(payload_str)
            if "driverguard/alerts" in topic:
                await self._handle_alert(data)
            elif "driverguard/telemetry" in topic:
                await self._handle_telemetry(data)
        except Exception as e:
            logger.error(f"Failed to process message on {topic}: {e}")

    async def _handle_alert(self, data: dict):
        async with get_db_context() as db:
            # 1. Lưu bản ghi SafetyAlertLog
            alert = SafetyAlertLog(
                id=data.get("event_id"),
                shift_id=data.get("shift_id"),
                vehicle_id=data.get("vehicle_id"),
                driver_id=data.get("driver_id"),
                alert_type=data.get("alert_type"),
                severity=data.get("severity"),
                latitude=data.get("telemetry", {}).get("latitude"),
                longitude=data.get("telemetry", {}).get("longitude"),
                speed_kmh=data.get("telemetry", {}).get("speed_kmh", 0),
                ear_value=data.get("biometrics", {}).get("ear"),
                perclos_value=data.get("biometrics", {}).get("perclos_60s"),
                snapshot_url=data.get("evidence", {}).get("snapshot_url")
            )
            db.add(alert)

            # 2. Nếu là vi phạm nặng -> Tự động đưa vào hàng chờ HITL
            if alert.severity in [AlertSeverity.HIGH, AlertSeverity.CRITICAL]:
                hitl_case = HitlReviewCase(alert_id=alert.id)
                db.add(hitl_case)

            await db.commit()

        # 3. Đẩy realtime xuống Web Dashboard qua SSE
        await sse_broadcaster.broadcast(event="alert", data=data)

    async def _handle_telemetry(self, data: dict):
        # Phát sóng vị trí xe cho bản đồ trực tiếp trên Web
        await sse_broadcaster.broadcast(event="gps", data=data)

mqtt_service = MqttIngestionService()
```
