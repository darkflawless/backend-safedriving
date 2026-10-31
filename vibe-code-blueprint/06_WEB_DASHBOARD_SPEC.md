# 🖥️ 06. WEB DASHBOARD & HITL OPS SPECIFICATION
> **Đặc Tả Giao Diện Trung Tâm Giám Sát Đội Xe (React/Vite), Bản Đồ Trực Tiếp, và Cổng Duyệt HITL**

---

## 1. PHONG CÁCH THIẾT KẾ (DESIGN SYSTEM)

- **Chủ đề (Theme)**: Giao diện Trung tâm Điều hành Tối tân (Dark Command Center).
  - Nền chính: `#0B0F19` (Deep Navy / Midnight)
  - Nền thẻ / Container: `#161F30` với hiệu ứng kính mờ (Glassmorphism, border `rgba(255, 255, 255, 0.08)`)
  - Màu nhấn trạng thái:
    - Xanh an toàn (Safe/Active): `#10B981` (Emerald)
    - Vàng cảnh báo (Warning): `#F59E0B` (Amber)
    - Đỏ nguy hiểm (Critical/Danger): `#EF4444` (Crimson Red)
    - Xanh thương hiệu: `#3B82F6` (Electric Blue)
- **Bộ icon**: `lucide-react` (Car, ShieldAlert, Video, UserCheck, Activity, MapPin, Eye, Bell).

---

## 2. CÁC MÀN HÌNH CHÍNH (PAGES & VIEWS)

```
[Sidebar Navigation]
  ├── 📊 Tổng Quan Hạm Đội (Fleet Command Overview)
  ├── 🗺️ Bản Đồ Vị Trí Thời Gian Thực (Live Fleet Map)
  ├── 🚨 Cảnh Báo An Toàn & Lịch Sử (Safety Alert Logs)
  ├── ⚖️ Cổng Duyệt Vi Phạm (HITL Review Center)
  └── 🚗 Quản Lý Phương Tiện & Tài Xế (Vehicles & Drivers)
```

---

## 3. CƠ CHẾ KẾT NỐI SSE REALTIME TRÊN REACT (`useRealtimeAlerts`)

Hook mẫu chuẩn công nghiệp lắng nghe sự kiện từ Backend, tự động kết nối lại khi mất mạng và phát âm thanh cảnh báo:

```typescript
// src/hooks/useRealtimeAlerts.ts
import { useEffect, useRef, useState } from 'react';

export interface AlertEvent {
  id: string;
  vehicle_id: string;
  plate_number: string;
  driver_name: string;
  alert_type: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  timestamp: string;
  speed_kmh: number;
  ear?: number;
  snapshot_url?: string;
}

export function useRealtimeAlerts(token: string) {
  const [alerts, setAlerts] = useState<AlertEvent[]>([]);
  const [latestAlert, setLatestAlert] = useState<AlertEvent | null>(null);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    // Chuẩn bị âm thanh báo động
    audioRef.current = new Audio('/sounds/emergency_alarm.mp3');

    const eventSource = new EventSource(`/api/v1/realtime/stream?token=${token}`);

    eventSource.addEventListener('alert', (e) => {
      try {
        const newAlert: AlertEvent = JSON.parse(e.data);
        setLatestAlert(newAlert);
        setAlerts((prev) => [newAlert, ...prev.slice(0, 99)]); // Giữ tối đa 100 sự kiện gần nhất

        // Phát chuông báo động nếu vi phạm nghiêm trọng
        if (newAlert.severity === 'CRITICAL' || newAlert.severity === 'HIGH') {
          audioRef.current?.play().catch(() => {
            console.log('Autoplay audio blocked by browser policy');
          });
        }
      } catch (err) {
        console.error('Error parsing SSE alert:', err);
      }
    });

    eventSource.addEventListener('gps', (e) => {
      // Cập nhật vị trí marker trên bản đồ
    });

    eventSource.onerror = (err) => {
      console.warn('SSE connection closed. Reconnecting...', err);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, [token]);

  return { alerts, latestAlert };
}
```

---

## 4. GIAO DIỆN BẢN ĐỒ ĐỘI XE THỜI GIAN THỰC (LEAFLET LIVE MAP)

- Thư viện: `react-leaflet` hoặc `leaflet` thuần.
- Marker hiển thị biểu tượng ô tô với màu sắc tương ứng:
  - **Xanh**: Xe đang chạy bình thường, tài xế tỉnh táo.
  - **Vàng**: Có dấu hiệu mệt mỏi / ngáp (Yawn).
  - **Đỏ chớp tắt**: Cảnh báo ngủ gật (Drowsiness) hoặc mất tập trung nghiêm trọng.
- Khi bấm vào xe: Hiện popup thông tin chi tiết:
  - Biển số, Loại xe, Tên tài xế
  - Tốc độ hiện tại ($km/h$)
  - Trạng thái sinh trắc học: EAR, MAR, PERCLOS
  - Nút xem nhanh camera hoặc gọi điện thoại khẩn cấp.

---

## 5. CỔNG DUYỆT VI PHẠM TẬP TRUNG (HUMAN-IN-THE-LOOP INTERFACE)

Màn hình cho phép Điều phối viên xem xét các vi phạm do AI gắn cờ:

```
+-------------------------------------------------------------------------------+
| HITL VIOLATION REVIEW PANEL                                  [Trạng thái: 3 chờ] |
+-------------------------------------------------------------------------------+
| [Danh sách sự kiện]     | [Chi tiết xem xét]                                  |
|                         |                                                     |
| > 02:35 Ngủ gật (30G)   | Ảnh chụp bằng chứng từ Camera trên xe:              |
|   02:14 Cúi đầu (29E)   | +-------------------------------------------------+ |
|   01:50 Dùng ĐT (51K)   | |                                                 | |
|                         | |        [ẢNH CHỤP TÀI XẾ NHẮM MẮT]               | |
|                         | |                                                 | |
|                         | +-------------------------------------------------+ |
|                         |                                                     |
|                         | Thông tin sinh trắc:                                |
|                         | - EAR: 0.14 (Ngưỡng nhắm: < 0.22)                   |
|                         | - Thời gian nhắm: 2.4 giây                          |
|                         | - Tốc độ xe: 65 km/h                                |
|                         |                                                     |
|                         | Ghi chú người duyệt:                                |
|                         | [Tài xế có dấu hiệu ngủ gật rõ rệt..............]   |
|                         |                                                     |
|                         | Phím tắt: [A] Duyệt vi phạm   | [R] Bác bỏ vi phạm   |
|                         | +--------------------+    +-----------------------+ |
|                         | | [Xác Nhận Vi Phạm] |    | [Bác Bỏ (Sai Số AI)]  | |
|                         | +--------------------+    +-----------------------+ |
+-------------------------------------------------------------------------------+
```

### Logic Phê Duyệt Nhanh Bằng Bàn Phím
- Bấm phím **`A`** (Approve): Gửi API duyệt vi phạm `APPROVED`, trừ điểm an toàn tài xế, tự động chuyển sang ca vi phạm tiếp theo.
- Bấm phím **`R`** (Reject): Gửi API bác bỏ `REJECTED`, đánh dấu là AI False Positive để sau này tinh chỉnh lại ngưỡng mô hình.
