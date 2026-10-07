# 🛠️ 01. ARCHITECTURE & INFRASTRUCTURE SPECIFICATION
> **Thiết lập Hạ tầng Container hóa Docker, Biến Môi Trường, và Cầu Nối Mạng Thiết Bị Thật**

---

## 1. DOCKER COMPOSE FILE MẪU (`docker-compose.yml`)

File cấu hình triển khai toàn bộ 4 dịch vụ cốt lõi: Backend, PostgreSQL, EMQX Broker, MinIO Storage.

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: driverguard_postgres
    restart: unless-stopped
    environment:
      POSTGRES_USER: ${POSTGRES_USER:-driverguard_user}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-driverguard_password}
      POSTGRES_DB: ${POSTGRES_DB:-driverguard_db}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U driverguard_user -d driverguard_db"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - driverguard_net

  emqx:
    image: emqx/emqx:5.3.2
    container_name: driverguard_emqx
    restart: unless-stopped
    ports:
      - "1883:1883"     # MQTT TCP
      - "8083:8083"     # MQTT WebSocket
      - "18083:18083"   # EMQX Dashboard Web
    environment:
      EMQX_DASHBOARD__DEFAULT_PASSWORD: ${EMQX_ADMIN_PASSWORD:-admin123}
      EMQX_ALLOW_ANONYMOUS: "true"
    volumes:
      - emqx_data:/opt/emqx/data
    healthcheck:
      test: ["CMD", "/opt/emqx/bin/emqx", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - driverguard_net

  minio:
    image: minio/minio:latest
    container_name: driverguard_minio
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ROOT_USER:-minioadmin}
      MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD:-minioadmin123}
    ports:
      - "9000:9000"     # S3 API
      - "9001:9001"     # MinIO Web Console
    volumes:
      - minio_data:/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - driverguard_net

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: driverguard_backend
    restart: unless-stopped
    ports:
      - "8000:8000"
    environment:
      - APP_ENV=production
      - DATABASE_URL=postgresql+psycopg://driverguard_user:driverguard_password@postgres:5432/driverguard_db
      - MQTT_BROKER_HOST=emqx
      - MQTT_BROKER_PORT=1883
      - MQTT_BROKER_ENDPOINT=tcp://192.168.112.72:1883
      - S3_ENDPOINT_URL=http://minio:9000
      - S3_ACCESS_KEY=minioadmin
      - S3_SECRET_KEY=minioadmin123
      - S3_BUCKET_NAME=driverguard-media
      - JWT_SECRET_KEY=your-super-secret-key-min-32-chars-long
    depends_on:
      postgres:
        condition: service_healthy
      emqx:
        condition: service_healthy
      minio:
        condition: service_healthy
    networks:
      - driverguard_net

volumes:
  postgres_data:
  emqx_data:
  minio_data:

networks:
  driverguard_net:
    driver: bridge
```

---

## 2. DOCKER COMPOSE OVERRIDE (`docker-compose.override.yml`)
> **Mục đích**: Tránh xung đột port `5432` nếu trên máy host đã có sẵn PostgreSQL khác đang chạy.

```yaml
version: '3.8'
services:
  postgres:
    ports: !override
      - "5433:5432"
```

---

## 3. DOCKERFILE CHO FASTAPI BACKEND

```dockerfile
FROM python:3.11-slim

WORKDIR /app

# Cài đặt curl để hỗ trợ healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Cài đặt dependencies vào /usr/local để bất kỳ user nào cũng đọc được (tránh Permission Denied)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Tạo user không có quyền root vì lý do an toàn
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8000

CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

---

## 4. BIẾN MÔI TRƯỜNG TOÀN DIỆN (`.env.example`)

```ini
# Application Core
APP_NAME=DriverGuard Safety Core
APP_ENV=development
DEBUG=True
API_V1_STR=/api/v1
CORS_ORIGINS=http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173

# Database (PostgreSQL)
# Lưu ý: Nếu kết nối từ Docker Backend tới Postgres container thì dùng host 'postgres:5432'
# Nếu kết nối từ Host bên ngoài vào Docker thì dùng 'localhost:5433'
DATABASE_URL=postgresql+psycopg://driverguard_user:driverguard_password@postgres:5432/driverguard_db

# Security & JWT
JWT_SECRET_KEY=c3f7b884d635032a9e224e76c1234567890abcdef1234567890abcdef123456
JWT_ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=1440

# MQTT IoT Broker (EMQX)
MQTT_BROKER_HOST=emqx
MQTT_BROKER_PORT=1883
# Điểm kết nối được gửi về cho Điện Thoại Android (Dùng IP Wi-Fi của máy tính)
MQTT_BROKER_ENDPOINT=tcp://192.168.112.72:1883
MQTT_KEEP_ALIVE=60
MQTT_USERNAME=
MQTT_PASSWORD=

# MinIO / AWS S3
S3_ENDPOINT_URL=http://minio:9000
# Endpoint cho Mobile hoặc Browser xem ảnh (Public URL)
S3_PUBLIC_URL=http://192.168.112.72:9000
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin123
S3_BUCKET_NAME=driverguard-media
S3_REGION=us-east-1
```

---

## 5. THIẾT LẬP CẦU NỐI MẠNG CHO THIẾT BỊ DI ĐỘNG THẬT (WI-FI LAN BRIDGE)

Khi test bằng điện thoại thật (Physical Android Phone), điện thoại và máy tính phải **bắt chung 1 mạng Wi-Fi**.

### Bước 1: Tìm IP Wi-Fi của Máy Tính
Trên Windows PowerShell:
```powershell
ipconfig
# Tìm mục "Wireless LAN adapter Wi-Fi" -> Xem dòng IPv4 Address (ví dụ: 192.168.112.72)
```

### Bước 2: Cấu hình trên Android Studio (`mobile/local.properties`)
```properties
DRIVERGUARD_API_BASE_URL=http://192.168.112.72:8000
```
Trong `app/build.gradle.kts`:
```kotlin
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

val apiBaseUrl = localProperties.getProperty("DRIVERGUARD_API_BASE_URL") ?: "http://10.0.2.2:8000"

defaultConfig {
    buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
}
```

### Bước 3: Mở Cổng Firewall Windows (Quan trọng)
Nếu điện thoại không ping được máy tính, mở PowerShell với quyền Administrator và chạy:
```powershell
New-NetFirewallRule -DisplayName "DriverGuard Backend" -Direction Inbound -LocalPort 8000 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "DriverGuard EMQX MQTT" -Direction Inbound -LocalPort 1883 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "DriverGuard MinIO" -Direction Inbound -LocalPort 9000 -Protocol TCP -Action Allow
```
